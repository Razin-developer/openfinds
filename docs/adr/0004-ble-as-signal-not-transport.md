# ADR 0004: BLE as a presence signal, not a pairing transport

## Status
Accepted.

## Context
The spec calls for "BLE + NSD (mDNS) + UDP Discovery." BLE advertisement payloads are small (tens of bytes) and, critically, BLE alone doesn't give two devices an IP address and port to open a TCP connection on — that still requires both devices to share a Wi-Fi network and use NSD or the UDP beacon.

## Decision
Use BLE purely as a low-power, no-network-required "an OpenFind device is nearby" signal: `BleAdvertiser` broadcasts a fixed OpenFind service UUID (no host/port/identity data), and `BleScanner` reports whenever that UUID is seen. This is surfaced to the user in the pairing screen as an informational hint ("A nearby device was also detected over Bluetooth") but never fabricated into a connectable `DiscoveredDevice` entry, since OpenFind has no host/port to offer for it.

## Consequences
- Honest UX: BLE detections never produce a "Pair" button that can't actually connect.
- Real pairing and all data transfer still requires both devices on the same Wi-Fi network (NSD/UDP for discovery, TCP for the P2P protocol) — BLE doesn't change that requirement, it just tells the user proximity was detected before Wi-Fi discovery catches up.
