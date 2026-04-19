# Zodl Android — UI Screen Review

Full inventory of every composable screen/flow in `ui-lib/src/main/java/.../screen/`. Intended for UX redesign planning — describes what each screen does without prescribing changes.

---

## Table of Contents

1. [Navigation Shell](#1-navigation-shell)
2. [Onboarding & Wallet Setup](#2-onboarding--wallet-setup)
3. [Authentication](#3-authentication)
4. [Home Dashboard](#4-home-dashboard)
5. [Receive](#5-receive)
6. [Send](#6-send)
7. [Pay (Incoming Payment Request)](#7-pay-incoming-payment-request)
8. [Swap / Exchange](#8-swap--exchange)
9. [Transactions](#9-transactions)
10. [Chat / Messaging](#10-chat--messaging)
11. [Address Book & Contacts](#11-address-book--contacts)
12. [QR & Scanning](#12-qr--scanning)
13. [Settings & Configuration](#13-settings--configuration)
14. [Hardware Wallet (Keystone)](#14-hardware-wallet-keystone)
15. [Wallet Management](#15-wallet-management)
16. [System / Utility Screens](#16-system--utility-screens)

---

## 1. Navigation Shell

### Tabs (`screen/tabs`)

The root shell of the app. Hosts a floating pill-shaped bottom nav bar with three top-level tabs.

| Tab | Content |
|-----|---------|
| **Wallet** (`WalletTabContent`) | Checks wallet state — shows loading spinner, a wallet setup screen (create/import), or the full Home dashboard |
| **Chat** | P2P encrypted messaging hub (direct from HomeScreen routing) |
| **Settings** (`SettingsTabContent`) | Identity card (avatar + display name + truncated public key), Security section (profile/identity, backup), Wallet section (seed phrase, server, advanced settings), About section, and danger-zone actions |

The Settings tab shows identity only if a Zapp P2P identity exists, and wallet sections only if a wallet is set up.

---

## 2. Onboarding & Wallet Setup

### Onboarding (`screen/onboarding`)

**Entry point for new users.** Full-screen gradient background with the app logo centered. Two primary CTAs:
- **Import Existing Wallet** — leads to the seed-phrase restore flow
- **Create New Wallet** — generates a new wallet

### Wallet Backup / Seed Phrase (`screen/walletbackup`)

Shows the user's **24-word BIP-39 seed phrase**. Screen is secured (screenshots blocked, screen content hidden from recent apps). The seed words are displayed in a grid layout with a blurring effect until the user taps to reveal. Includes:
- Help info icon
- A "copy" or "share" action

Used both during initial backup and when the user wants to view their recovery phrase later.

### Restore — Seed Entry (`screen/restore/seed`)

A dedicated multi-word text field for entering all **24 seed phrase words**. Features:
- Word-by-word auto-complete chip suggestions displayed in a horizontal scrollable row above the keyboard
- Paste support
- Navigation between word fields
- Validation error display
- "Continue" button enabled only when all 24 words are valid

### Restore — Birthday Date (`screen/restore/date`)

**Optional step.** Asks the user for the approximate date their wallet was created (so the app can scan from that block height instead of genesis). Provides a date picker.

### Restore — Birthday Height (`screen/restore/height`)

**Optional alternative.** Lets the user enter a specific block height number directly instead of a date. Useful for advanced users who know the exact starting block.

### Restore — Estimation (`screen/restore/estimation`)

**Informational screen.** Shown after birthday selection. Displays an estimated time to complete the blockchain sync based on the birthday input. Lets the user confirm before starting.

### Restore — Tor Option (`screen/restore/tor`)

Asks the user whether to route the restore sync through the **Tor network** for enhanced privacy. Shown during the restore flow before syncing begins.

### Restore Success (`screen/restoresuccess`)

**Confirmation screen** shown after a wallet is successfully restored. Celebrates the import with a success visual and prompts the user to proceed into the app.

---

## 3. Authentication

### App Access Authentication (`screen/authentication`)

**Lock/unlock screen.** Shown when the app requires biometric or PIN authentication to proceed. Displays:
- A welcome animation (Lottie)
- A retry button if authentication fails
- An error dialog with error code and message, offering "Retry" or "Contact Support" options

This is not a login screen — it's a local device auth gate to prevent unauthorized access to the wallet.

---

## 4. Home Dashboard

### Home (`screen/home`)

The **main wallet dashboard**. Composed of several stacked widgets:

- **Top app bar** with account switcher (supports multiple accounts)
- **Balance Widget** — shows total ZEC balance (shielded + transparent), with optional fiat equivalent. Tappable to show/hide the balance
- **Navigation buttons row** — 4–5 large icon buttons: Receive, Send, Scan, Buy (and optionally a 5th). Slightly overlaps the content below
- **Balance Chart Widget** — sparkline or bar chart showing balance history over time
- **Activity Widget** — a preview list of recent transactions (last few) with a "See All" link

### Balance Widget (`screen/balances`)

Reusable widget showing the user's current ZEC balance. Appears on both Home and Send screens. Breaks down:
- Total balance
- Spendable (confirmed shielded) balance (shown as a separate tappable element)
- Exchange rate converted value (when opted in)

### Spendable Balance (`screen/balances/spendable`)

A modal or sheet that explains the difference between total balance and spendable balance — e.g., pending/unconfirmed funds aren't spendable yet.

### Balance Chart (`screen/home/balancechart`)

A visual chart widget embedded in the Home scroll content. Shows balance history (sparkline or bar). Purely informational — no interactive controls.

### Home State Banners (`screen/home/*`)

The home screen can show inline banners/messages for various wallet states:
- **Syncing** — progress indicator showing blockchain sync percentage
- **Restoring** — restoration progress
- **Disconnected** — no network or server connection
- **Shield Funds** — prompt to shield transparent funds
- **Error** — inline wallet error message with action button
- **Tor** — Tor status notice
- **Updating** — app update in progress
- **Backup reminder** — nudge to back up seed phrase

---

## 5. Receive

### Receive (`screen/receive`)

Shows the user's **receiving addresses** as a vertical list of expandable panels. Each address panel shows:
- Icon (shielded indicator badge if applicable)
- Address type title and subtitle
- Info icon button

When expanded, shows three action buttons:
- **Copy** (shielded addresses only)
- **QR Code** — opens the QR code display
- **Request** — opens the payment request flow

Supports multiple address types (shielded Zodl, transparent, Keystone hardware wallet) with distinct color themes per type. Bottom note recommends preferring shielded addresses.

### Shielded Address Info (`screen/receive/info/ShieldedAddressInfoScreen`)

Info sheet explaining what a shielded address is and why it offers more privacy.

### Transparent Address Info (`screen/receive/info/TransparentAddressInfoScreen`)

Info sheet explaining what a transparent address is and its privacy trade-offs.

### QR Code Display (`screen/qrcode`)

Full-screen display of a wallet address as a QR code. Used when "QR Code" is tapped on the Receive screen. Includes share functionality.

### Request (`screen/request`)

A multi-step flow to **create a payment request QR code**:

1. **Amount step** — enter the requested amount in ZEC or fiat, with currency toggle chip
2. **Memo step** — optionally add a text memo
3. **QR Code step** — displays the final payment request QR code encoding address + amount + memo; user can share it

---

## 6. Send

### Send (`screen/send`)

The primary **send ZEC form**. Contains:
- Balance widget at the top showing current balance
- **Recipient address field** — accepts typed input, scanned QR, or address book selection. Shows validation state (shielded/transparent/invalid indicator)
- **Amount field** — enter ZEC amount with optional fiat conversion display; "Send All" shortcut available
- **Memo field** — optional private note (only for shielded recipients). Shows character limit
- **Address book hint** — if a known contact matches, shows their name as a chip above the address field
- "Confirm" / "Send" button

### Review Transaction (`screen/reviewtransaction`)

**Pre-send confirmation screen.** Shows a full summary before submitting:
- Token amount (large auto-sizing display)
- Fiat equivalent
- Recipient address (truncated/styled)
- Memo (if any)
- Fee
- For swap transactions: from/to asset pair, exchange rate, slippage

Has a "Confirm" button at the bottom. Can also show a memo field for last-minute note editing.

### Transaction Progress (`screen/transactionprogress`)

**Post-send status screen.** Full-screen result with three visual states:
- **Pending** — indigo gradient + animation indicating transaction is broadcasting
- **Success** — green gradient + success animation
- **Error** — red gradient + error icon and message

Shows transaction amount, recipient, and status text. Bottom bar has action buttons (e.g. "Done", "Try Again", "View on Explorer").

---

## 7. Pay (Incoming Payment Request)

### Pay (`screen/pay`)

The **"pay someone's request"** form. Structurally similar to Send but pre-populated from an incoming payment request URI. Contains:
- Asset card showing the currency being sent
- Amount field (may be pre-filled, locked, or editable depending on the request)
- Recipient address field
- Optional memo field
- Address book / scan quick-actions
- "Review" button

Supports both ZEC and cross-chain token payments (for swap-based requests).

### Pay Info (`screen/pay/info`)

An informational bottom sheet or screen explaining what a "pay" action entails — used to clarify the difference between a normal send and fulfilling a payment request.

---

## 8. Swap / Exchange

### Swap (`screen/swap`)

The main **cross-chain swap form**. Lets the user exchange ZEC for another crypto asset. Contains:
- "You send" section: amount input + asset selector card
- "You receive" section: estimated output amount + target asset selector card (with swap direction toggle)
- Recipient address field for the destination chain
- Slippage tolerance chip/button
- Optional memo field
- "Get Quote" / "Review" button

### Swap Quote (`screen/swap/quote`)

A **bottom sheet** that appears after requesting a quote. Shows:
- From/to token pair with amounts
- Exchange rate
- Estimated fee
- Slippage setting
- Expiry countdown
- "Confirm Swap" and "Cancel" buttons

Error variant shown when no quote is available.

### Review Swap / OR Swap Confirmation (`screen/swap/orconfirmation`)

Confirmation screen for an "order-routing" swap. Shows the full swap details — input asset, output asset, amounts, fees, deposit address — before the user commits funds.

### Swap Detail (`screen/swap/detail`)

**Post-swap status detail screen.** Shows the live status of an in-progress or completed swap transaction. Layout mirrors the Transaction Detail screen but with swap-specific fields:
- From/to amounts and assets
- Swap status (expandable row with steps: initiated → deposited → exchanging → complete/refunding)
- Deposit address
- Timestamps

Includes a "Get Support" button for failed swaps.

### Swap Asset Picker (`screen/swap/picker`)

A searchable list of supported crypto assets to swap into. Used when tapping the "receive" asset selector in the Swap form.

### Swap Blockchain Picker (`screen/swap/picker`)

Selects the blockchain/network for the destination asset (e.g. ERC-20 vs BEP-20).

### Swap Slippage (`screen/swap/slippage`)

Picker screen for setting slippage tolerance. Shows preset options (0.1%, 0.5%, 1%) plus a custom input field.

### Swap Info (`screen/swap/info`)

Info sheet explaining how the swap works, including fees, timing, and what happens if the swap fails.

### Swap Refund Address Info (`screen/swap/info`)

Info sheet explaining what the refund address is and why it is needed (in case the swap fails and funds need to be returned).

### Ephemeral Lock (`screen/swap/lock`)

An intermediate screen shown when a swap requires the user to remain in-app during a time-sensitive window. Prevents navigation away until the lock period expires.

### Swap Support (`screen/swap/detail/support`)

A support contact screen linked from a failed swap. Shows context about the failed transaction and provides a way to reach support.

### Add/Select Swap Address Book Contact (`screen/swap/ab`)

Address book flows specific to the Swap recipient field — add a new cross-chain contact or select an existing one.

---

## 9. Transactions

### Transaction History (`screen/transactionhistory`)

The **full transaction list** screen. Accessed via "See All" from the Home activity widget. Features:
- Search bar to filter by address, memo, or amount
- Filter button (opens Transaction Filters)
- Lazy scrollable list grouped by date headers
- Each row shows: direction icon, contact name or address, amount, timestamp, status badge
- Empty state with illustration

### Transaction Filters (`screen/transactionfilters`)

A modal or sheet for filtering the transaction history list. Filter options include:
- Transaction type (sent, received, shield, swap)
- Date range

### Transaction Detail (`screen/transactiondetail`)

Full detail view for a single transaction. Layout varies by transaction type:

| Type | Key fields shown |
|------|-----------------|
| **Sent (shielded)** | Amount, memo, recipient address, fee, block, confirmation count |
| **Sent (transparent)** | Amount, recipient address, fee, block |
| **Received (shielded)** | Amount, memo, sender (if known), block |
| **Received (transparent)** | Amount, sender address, block |
| **Shielding** | Amount shielded, fee, block |
| **Sent via Swap** | From/to amounts, swap status, deposit address |

Top has a gradient background. Bookmark button lets users save/note the transaction. Bottom bar may show retry or support buttons for failed transactions.

### Transaction Note (`screen/transactionnote`)

Lets the user **add or edit a private local note** on any transaction. The note is stored locally (not on-chain). Simple text field with save/delete actions.

---

## 10. Chat / Messaging

### Chat Room (`screen/chat/view`)

The **P2P encrypted direct message screen**. Uses Hyperswarm + Autobase under the hood. UI is a full-screen chat interface:

**Top bar:**
- Conversation display name
- Real-time connection status subtitle (Peer online / Peer offline — messages will queue / Connecting / DHT unreachable)
- Connection pill indicator (tappable for network details)

**Message list:**
- Reverse-chronological lazy column
- Each message renders as a typed bubble:

| Content type | Bubble |
|---|---|
| `text/plain` | Standard chat bubble with timestamp + delivery indicator (sent/queued/sending/failed) |
| `application/payment-request` | Payment request card bubble |
| `application/wallet-address` | Wallet address share bubble |
| `application/zec-transaction` | Transaction reference bubble |
| `application/location` | Location map pin bubble |
| `image/*` or `video/*` | Media preview bubble |
| File | File attachment bubble |

**Input area:**
- Attachment "+" button (opens Attachment Sheet)
- Multi-line text field (max 4 lines)
- Send button (enabled when text is non-empty)

**Attachment sheet options:**
- Share my wallet address
- Send ZEC (navigates to Send screen)
- Attach media → opens Media sub-sheet

**Media sub-sheet options:**
- Choose photo/video from gallery
- Attach any file
- Take photo (requests camera permission)
- Share current location (requests location permission)

**Network Details Sheet:** tappable pill opens a debug sheet showing P2P connection details (peer count, DHT health, node info).

### Chat Scan Public Key (`screen/chat/scan`)

QR scanner specific to scanning another user's **Zapp P2P public key** to start a conversation. Uses the camera to decode a public key QR code.

---

## 11. Address Book & Contacts

### Address Book (`screen/addressbook`)

A scrollable **contact list** used in two modes:
1. **Standalone address book** — manage all saved contacts
2. **Select Recipient** — opened from Send/Pay to choose a contact as the transaction recipient

Features:
- Alphabetically grouped list with section headers
- Each contact row shows avatar initials, name, address (truncated, styled), shielded badge
- Empty state with dashed-border card and illustration
- "Add Contact" button at the bottom (opens a popup with "Scan" or "Manual" options)
- Info icon button (shown in recipient-selection mode to explain the context)

### Add / Edit Contact (`screen/contact`)

Form to **create or update an address book entry**. Fields:
- Wallet address (validated text field with paste/scan capability)
- Contact name
- Optional chain selector (picker for Keystone / multi-chain contacts)

Actions: Save / Delete (destructive button, shown only when editing).

---

## 12. QR & Scanning

### Scan (`screen/scan`)

Full-screen **camera QR scanner** with three variants:
- **Scan Zcash Address** — validates that the scanned code is a valid Zcash address
- **Scan Generic Address** — accepts any address format (used in Swap/Pay for cross-chain)
- **Third-Party Scan** — handles incoming scan requests from external apps (deeplink)

Shows a viewfinder overlay, torch/flash toggle, and image picker fallback (scan from gallery). Validation errors shown inline below the viewfinder.

---

## 13. Settings & Configuration

### More / Settings (`screen/more`)

The **secondary settings menu**, accessible from within the wallet flow (separate from the tab-based Settings). A simple scrollable list of items with icons:
- Privacy / security items
- Support link
- About link
- App version display (long-press / double-tap reveals debug info)

### Advanced Settings (`screen/advancedsettings`)

A focused settings sub-menu with list items:
- Recovery phrase (view seed)
- Export private data
- Choose server
- Currency conversion
- Crash reporting

Bottom of the screen has an info note and a red **"Delete Wallet"** destructive button.

### Choose Server (`screen/chooseserver`)

Lets the user **select a lightwalletd server** for blockchain sync. Two sections:
- **Fastest Servers** — auto-detected fastest endpoints (with a refresh/retry button and loading animation while probing latency)
- **Other Servers** — full list of known servers

Each server is a radio button. The custom server entry expands inline to show a URI text field. A "Save" button at the bottom applies the selection. Shows a validation error dialog if the server is unreachable.

### Exchange Rate Opt-In (`screen/exchangerate/optin`)

**First-time opt-in screen** for fiat currency conversion. Shows:
- Header image
- Description of what the feature does
- Two info rows explaining data source and privacy implications
- "Enable" (primary) and "Skip" (text button) CTAs

### Exchange Rate Settings (`screen/exchangerate/settings`)

Toggle screen to turn the exchange rate feature on or off after initial opt-in. Shows the current opt-in/opt-out state with radio options and a "Save" button (disabled if the current selection matches the saved state).

### Tor Opt-In (`screen/tor/optin`)

**Privacy feature opt-in.** Explains the Tor network and its benefits with three info rows. "Enable Tor" (primary) and "No Thanks" (text button) CTAs.

### Tor Settings (`screen/tor/settings`)

Post-opt-in toggle screen for enabling/disabling Tor. Same layout pattern as Exchange Rate Settings.

### Crash Reporting Opt-In (`screen/crashreporting`)

Screen with two radio-card options:
- **Share crash reports** (opt in)
- **Don't share** (opt out)

Shows a "Save" button (disabled until selection differs from current state). Includes an info note about what data is collected.

### About (`screen/about`)

App information screen showing:
- App subtitle and short description
- Privacy Policy link (list item)
- Terms of Use link (list item)
- App version number at the bottom
- Debug build: overflow menu with git SHA and config info

### What's New (`screen/whatsnew`)

**Changelog / release notes** screen. Shows the current version number and date header, then a list of sections (features, fixes) with bullet points. Content is selectable text.

### Support / Feedback (`screen/support`)

A form or link for contacting support. Collects device/app diagnostic info automatically (version, network state) and lets the user write a message.

---

## 14. Hardware Wallet (Keystone)

### Connect Keystone (`screen/connectkeystone`)

**Setup wizard** for adding a Keystone hardware wallet. Shows:
- Keystone brand logo
- Title and subtitle
- A numbered list of 4 steps explaining how to connect the device (export public key via QR, etc.)
- "Continue" button

### Scan Keystone (`screen/scankeystone`)

Camera QR scanner specific to reading the **Keystone device's exported public key or PSBT** QR codes. Used during initial pairing and during transaction signing.

### Select Keystone Account (`screen/selectkeystoneaccount`)

After scanning the Keystone, shows a **list of derived accounts** from the hardware wallet's HD key. User picks which account(s) to import into the app.

### Sign Keystone Transaction (`screen/signkeystonetransaction`)

Shows a **QR code** that the user must scan with their Keystone device to sign the transaction (PSBT flow). After signing, the user scans the Keystone's response QR back into the app.

### Disconnect Hardware Wallet (`screen/disconnect`)

Confirmation screen before removing a Keystone from the app. Shows:
- Title + explanatory text
- A bullet list of what will be lost (account access, pending transactions)
- A "currently connected" card showing the Keystone icon + online status badge
- Info note about data retention
- Red "Disconnect" destructive button
- Confirmation bottom sheet before executing

---

## 15. Wallet Management

### Delete Wallet / Reset (`screen/deletewallet`)

Wipes all local wallet data. Shows:
- Warning title and three explanatory paragraphs
- A highlighted warning card with a checkbox ("I understand this data cannot be recovered") — the delete button is disabled until checked
- Red "Confirm" button
- A confirmation bottom sheet as a final safety gate

### Export Private Data (`screen/exportdata`)

Allows exporting sensitive wallet data (keys/database). Shows:
- Explanatory text about what is exported and the risks
- An agreement checkbox
- "Export" button (disabled until agreed)
- Snackbar feedback on completion

### Tax Export (`screen/taxexport`)

Generates a **CSV export of transaction history** for tax reporting purposes. Shows a description of the export format and an action button to trigger the export/share.

### Resync — Confirm (`screen/resync/confirm`)

Confirmation before wiping and re-scanning the blockchain (erases local transaction cache). Shows what will be lost and asks the user to confirm.

### Resync — Birthday Date (`screen/resync/date`)

Lets the user optionally specify a wallet birthday date before resyncing (to limit scan range).

### Resync — Estimation (`screen/resync/estimation`)

Shows the estimated time the resync will take based on the selected birthday.

### Account List (`screen/accountlist`)

A **bottom sheet** showing all wallet accounts (e.g. Zodl account, Keystone account). Each item has an icon, account name, truncated address, and a selection highlight. Used to switch the active account.

---

## 16. System / Utility Screens

### Integrations (`screen/integrations`)

A **bottom sheet** listing third-party payment integrations available in the app (e.g. Flexa). Each integration is a list item with an icon and description. May show a "powered by" footer.

### Flexa (`screen/flexa`)

Wraps the **Flexa SDK** payment UI. Allows the user to spend ZEC at merchants using Flexa. The composable launches the Flexa SDK's own UI overlay.

### Insufficient Funds (`screen/insufficientfunds`)

A **bottom sheet** shown when the user tries to send/swap more than their spendable balance. Shows an explanatory image, title, description of why funds may not be available (e.g. unconfirmed/shielded), and a "Got it" dismiss button.

### TEX Unsupported (`screen/texunsupported`)

A **bottom sheet** shown when the user scans or enters a TEX (Transparent-Extended) Zcash address that the app doesn't support. Shows:
- A "TEX" badge label
- Explanation of why TEX addresses are not supported
- A dismiss button

### Ephemeral Hotfix (`screen/hotfix/ephemeral`)

A **bottom sheet** used for hotfix migrations that require user input — specifically prompting the user to enter an address (e.g. to migrate funds from a deprecated address format). Contains:
- Title + subtitle
- Address text field (auto-focused)
- Info note
- Action button

### Enhancement Hotfix (`screen/hotfix/enhancement`)

A system-triggered screen for app enhancement migrations. Runs a background process and shows progress or result to the user.

### Warning — Not Enough Space (`screen/warning`)

Shown when the device does not have sufficient storage to sync the blockchain. Explains the issue and suggests freeing up space before retrying.

### Sync Error Dialog (`screen/error`)

A modal dialog shown when the blockchain sync encounters a critical error. Shows the error type and options to retry or seek support.

---

## Summary Map

```
App
├── [Tab: Wallet]
│   ├── Wallet Setup (Onboarding → Create / Restore)
│   │   ├── Onboarding
│   │   ├── Restore: Seed → Date → Height → Estimation → Tor → Success
│   │   └── Authentication (biometric gate)
│   └── Home Dashboard
│       ├── Balance Widget + Chart
│       ├── Activity Widget (→ Transaction History)
│       └── Nav Buttons
│           ├── Receive → QR Code / Request
│           ├── Send → Review → Progress
│           ├── Scan (QR)
│           └── Buy / Swap → Quote → OR Confirmation → Detail
│
├── [Tab: Chat]
│   ├── Chat Room
│   │   ├── Attachment: Share Address / Send ZEC / Media / Location
│   │   └── Network Details Sheet
│   └── Scan Public Key (add new contact via QR)
│
├── [Tab: Settings]
│   ├── Identity Card (display name, public key)
│   ├── Security: Profile & Identity, Backup
│   ├── Wallet: Seed Backup, Server, Advanced Settings
│   │   ├── Advanced Settings → Recovery / Export / Server / Currency / Crash / Delete
│   │   ├── Choose Server
│   │   ├── Exchange Rate Opt-In / Settings
│   │   ├── Tor Opt-In / Settings
│   │   ├── Crash Reporting Opt-In
│   │   ├── Export Private Data
│   │   ├── Tax Export
│   │   ├── Resync → Confirm / Date / Estimation
│   │   └── Delete Wallet
│   ├── About
│   ├── What's New
│   └── Support
│
├── Address Book → Add/Edit Contact
├── Account List (bottom sheet)
├── Integrations (bottom sheet) → Flexa
├── Keystone Hardware Wallet
│   ├── Connect → Scan → Select Account → Sign → Disconnect
└── System / Utility
    ├── Insufficient Funds (sheet)
    ├── TEX Unsupported (sheet)
    ├── Ephemeral Hotfix (sheet)
    ├── Enhancement Hotfix
    ├── Storage Warning
    └── Sync Error Dialog
```
