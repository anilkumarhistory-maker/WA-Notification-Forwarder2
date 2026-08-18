# WA Notification Forwarder — Android

Purpose:
Mobile A = SOURCE. It reads notifications from two selected WhatsApp packages and forwards them over the internet.
Mobile B = RECEIVER. It listens for forwarded messages and shows them as Android notifications.

No username/password login.

How to use:
1. Build/install the APK on both phones.
2. Choose a long random private topic, e.g. `iengineer-wa-7f8c...` and enter the SAME topic on both phones.
3. On Mobile A choose SOURCE. Enter the two WhatsApp package names and the editable prefixes:
   - Sandhya msg received
   - Niti msg received
4. Open Notification Access and enable this app.
5. On Mobile B choose RECEIVER and use the same topic.
6. Keep Mobile B's receiver service active.

Important:
- The transport in this starter build uses ntfy.sh over HTTPS/SSE. This avoids needing your own server.
- Use a long random topic; topic names are bearer-like identifiers. Do not use a short/common topic.
- Android notification access must be granted manually.
- Battery optimization may need to be disabled for reliable background operation.
- WhatsApp/clone package names vary by phone. They must be entered correctly.
- This forwards notification content only; it does not read WhatsApp chats directly or send WhatsApp messages.
