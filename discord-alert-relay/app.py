import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib import error, request


DISCORD_WEBHOOK_URL = os.environ.get("DISCORD_WEBHOOK_URL", "").strip()
DISCORD_MENTION = os.environ.get("DISCORD_MENTION", "").strip()
PORT = int(os.environ.get("PORT", "5001"))
MAX_DISCORD_LENGTH = 1800
MAX_ALERT_LINES = 5


def truncate(text):
    if len(text) <= MAX_DISCORD_LENGTH:
        return text
    return text[: MAX_DISCORD_LENGTH - 3] + "..."


def build_message(payload):
    status = str(payload.get("status", "unknown")).upper()
    alerts = payload.get("alerts", [])

    lines = []
    if DISCORD_MENTION:
        lines.append(DISCORD_MENTION)

    lines.append(f"**Alertmanager status:** {status}")
    lines.append(f"**Alert count:** {len(alerts)}")

    for index, alert in enumerate(alerts[:MAX_ALERT_LINES], start=1):
        labels = alert.get("labels", {})
        annotations = alert.get("annotations", {})
        severity = labels.get("severity", "unknown")
        alert_name = labels.get("alertname", "unnamed-alert")
        summary = annotations.get("summary", "No summary")
        description = annotations.get("description", "")

        lines.append(f"{index}. [{severity}] {alert_name}")
        lines.append(f"   {summary}")
        if description:
            lines.append(f"   {description}")

    if len(alerts) > MAX_ALERT_LINES:
        lines.append(f"...and {len(alerts) - MAX_ALERT_LINES} more alert(s)")

    return truncate("\n".join(lines))


def send_to_discord(message):
    if not DISCORD_WEBHOOK_URL:
        raise RuntimeError("DISCORD_WEBHOOK_URL is not configured")

    body = json.dumps({"content": message}).encode("utf-8")
    discord_request = request.Request(
        DISCORD_WEBHOOK_URL,
        data=body,
        headers={
            "Content-Type": "application/json",
            "User-Agent": "spring-observability-study-discord-relay/1.0",
        },
        method="POST",
    )

    with request.urlopen(discord_request, timeout=10) as response:
        response.read()


class AlertHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get("Content-Length", "0"))
        raw_body = self.rfile.read(content_length)

        try:
            payload = json.loads(raw_body.decode("utf-8") or "{}")
        except json.JSONDecodeError:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"invalid json")
            return

        try:
            send_to_discord(build_message(payload))
        except (RuntimeError, error.URLError, error.HTTPError) as exception:
            self.send_response(500)
            self.end_headers()
            self.wfile.write(str(exception).encode("utf-8"))
            return

        self.send_response(204)
        self.end_headers()

    def log_message(self, format, *args):
        return


if __name__ == "__main__":
    server = ThreadingHTTPServer(("0.0.0.0", PORT), AlertHandler)
    print(f"discord relay listening on port {PORT}")
    server.serve_forever()
