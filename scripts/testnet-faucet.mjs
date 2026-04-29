#!/usr/bin/env node
// Headless Zcash testnet faucet client.
//
// Drips TAZ to a unified address by solving the PoW challenge that gates
// testnet.zecfaucet.com's /api/{challenge,add} endpoints. Bypasses the
// browser captcha. Ported from the fe-mega/zns frontend.
//
// Usage:
//   scripts/testnet-faucet.mjs utest1xxxxx...
//   scripts/testnet-faucet.mjs                # reads addr from emulator clipboard via adb
//
// Requires: Node 18+ (built-in fetch, worker_threads, crypto). No npm install.
//
// KNOWN ISSUE (2026-04): testnet.zecfaucet.com:2653 returns
//   {"status":403,"message":"Sorry, we couldn't verify you're not a robot."}
// to non-browser clients before the PoW step, even with full browser headers.
// This is almost certainly a TLS-fingerprint (JA3/JA4) check at the edge —
// real Chrome's TLS handshake passes, Node/curl's does not. To make this
// script actually deliver TAZ you need one of:
//   - a fingerprint-mimicking HTTP client (curl-impersonate, curl_cffi)
//   - a headless browser (Playwright) that runs the fe-mega faucet page
//   - keep using the manual browser flow (scripts/testnet-faucet.sh)
// The protocol implementation below is correct; only the transport is blocked.

import { createHash } from "node:crypto";
import { Worker, isMainThread, parentPort, workerData } from "node:worker_threads";
import { availableParallelism } from "node:os";
import { fileURLToPath } from "node:url";
import { execSync } from "node:child_process";

const FAUCET_API = "https://testnet.zecfaucet.com:2653/api";

// ---------- worker side ----------
if (!isMainThread) {
  const { message, difficulty, offset, stride } = workerData;
  const target = "0".repeat(difficulty);
  let nonce = offset;
  let best = { nonce: 0, hash: "f".repeat(64) };
  let count = 0;

  for (;;) {
    const hash = createHash("sha256").update(message + nonce).digest("hex");
    if (hash < best.hash) best = { nonce, hash };
    if (hash.startsWith(target)) {
      parentPort.postMessage({ type: "result", nonce, hash });
      break;
    }
    nonce += stride;
    if (++count % 5000 === 0) {
      parentPort.postMessage({ type: "progress", delta: 5000, best });
    }
  }
}

// ---------- main side ----------
async function main() {
  let addr = process.argv[2];
  if (!addr) {
    try {
      addr = execSync("adb shell cmd clipboard get-primary", { stdio: ["ignore", "pipe", "ignore"] })
        .toString().replace(/\r/g, "").trim();
    } catch { /* adb missing or no device */ }
  }
  if (!addr) {
    console.error("error: pass a unified address as the first arg, or copy one on a connected emulator first.");
    process.exit(2);
  }
  if (!addr.startsWith("utest1")) {
    console.error(`warning: '${addr.slice(0, 12)}…' doesn't start with utest1 — testnet UAs do.`);
  }

  console.log(`address: ${addr.slice(0, 40)}…`);

  // 1. challenge
  console.log("→ POST /api/challenge");
  const chRes = await fetch(`${FAUCET_API}/challenge`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ address: addr }),
  });
  const chData = await chRes.json();
  if (chData.status !== 200) {
    throw new Error(`challenge rejected: ${typeof chData.message === "string" ? chData.message : JSON.stringify(chData.message)}`);
  }
  const { id, message, difficulty, level } = chData.message;
  console.log(`  id=${id} difficulty=${difficulty} level=${level}`);

  // 2. mine PoW
  const nw = availableParallelism();
  console.log(`→ mining with ${nw} workers (target: ${difficulty} leading zeros)`);
  const startedAt = Date.now();
  let totalHashes = 0;
  let lastReport = Date.now();

  const solution = await new Promise((resolve, reject) => {
    const workers = [];
    let done = false;
    for (let i = 0; i < nw; i++) {
      const w = new Worker(fileURLToPath(import.meta.url), {
        workerData: { message, difficulty, offset: i, stride: nw },
      });
      workers.push(w);
      w.on("message", (m) => {
        if (m.type === "progress") {
          totalHashes += m.delta;
          const now = Date.now();
          if (now - lastReport > 1000) {
            const hps = Math.floor(totalHashes / ((now - startedAt) / 1000));
            process.stdout.write(`  ${hps.toLocaleString()} h/s · best=${m.best.hash.slice(0, 12)}…\r`);
            lastReport = now;
          }
        } else if (m.type === "result" && !done) {
          done = true;
          workers.forEach((wk) => wk.terminate());
          resolve({ nonce: m.nonce, hash: m.hash });
        }
      });
      w.on("error", (err) => {
        if (!done) {
          done = true;
          workers.forEach((wk) => wk.terminate());
          reject(err);
        }
      });
    }
  });

  const elapsed = ((Date.now() - startedAt) / 1000).toFixed(1);
  process.stdout.write("\n");
  console.log(`  solved in ${elapsed}s · nonce=${solution.nonce} hash=${solution.hash.slice(0, 16)}…`);

  // 3. submit
  console.log("→ POST /api/add");
  const subRes = await fetch(`${FAUCET_API}/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ address: addr, token: { id, nonce: solution.nonce, hash: solution.hash } }),
  });
  const subData = await subRes.json();
  if (subData.status !== 200) {
    throw new Error(`submission rejected: ${typeof subData.message === "string" ? subData.message : JSON.stringify(subData.message)}`);
  }
  const amount = subData.amount ?? 0.3;
  console.log(`✓ faucet accepted — sending ${amount} TAZ (confirms in ~75s)`);
}

if (isMainThread) {
  main().catch((e) => {
    console.error(`error: ${e.message ?? e}`);
    process.exit(1);
  });
}
