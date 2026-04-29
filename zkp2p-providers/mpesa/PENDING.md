# M-Pesa Provider — Deferred

**Status:** Blocked on Q6 — requires peer.xyz team confirmation before implementation.

## Question (Q6)

> Is M-Pesa confirmation data accessible via an HTTPS request/response that can be captured with Chrome DevTools MCP, or is the confirmation delivered via SMS only?

- If **HTTPS response** is available (e.g., M-Pesa web portal or Safaricom app web layer) → proceed with the `create-zkp2p-provider` skill and Chrome DevTools MCP using the same pattern as PIX/UPI/GCash.
- If **SMS-only** confirmation → defer until an alternative proof path (e.g., SMS notarization) is supported by the Reclaim proof engine.

## Markets gated on M-Pesa

| Country | ISO Code | Blocked in `PeerXyzUtil.UNSUPPORTED_REGIONS` |
|---------|----------|----------------------------------------------|
| Kenya   | KE       | Yes |
| Tanzania| TZ       | Yes |
| Uganda  | UG       | Yes |
| Ghana   | GH       | Yes (M-Pesa via Vodafone) |

Once the proof mechanism is confirmed and the provider is live, remove the corresponding country codes from `PeerXyzUtil.UNSUPPORTED_REGIONS` in:
`ui-lib/src/main/java/co/electriccoin/zcash/ui/common/util/PeerXyzUtil.kt`

## Candidate targets (if HTTPS available)

- **Safaricom M-Pesa portal** — `https://selfcare.safaricom.co.ke/` or `https://m-pesa.safaricom.co.ke/`
- **M-Pesa Global** — `https://sendmoney.co.ke/`

## Required proof fields

| Field | Description |
|-------|-------------|
| `transactionId` | M-Pesa confirmation code (e.g., `QEK12345AB`) |
| `amount` | Transfer amount in KES (or local currency) |
| `senderPhone` | Sender's mobile number |
| `recipientPhone` | Recipient's mobile number |
| `timestamp` | Transaction completion time |
| `status` | Must be `Completed` |
