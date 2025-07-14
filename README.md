# java-network-experiments
# Java Stateless Video Streaming Server

A lightweight, stateless video streaming server implemented in **pure Java**, inspired by the backend architecture of platforms like **YouTube**.

This server handles `.mp4` video streaming over HTTP using `Range` headers to support smooth browser-native playback, seeking, and pause/resume functionality — all without relying on heavy frameworks or external libraries.

---

## 🚀 Features

- **Serve `.mp4` video files** directly from a local folder
- Supports **HTTP `Range` requests** (chunked streaming)
- Example url : http://localhost:8000/video?name=video.mp4
