# socket-io-coroutines

![Build](https://github.com/T-Fowl/socket-io-coroutines/workflows/Build/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.tfowl.socketio/socket-io-coroutines)
![GitHub](https://img.shields.io/github/license/T-Fowl/socket-io-coroutines)

Simple kotlin coroutine extensions for the socket.io java client library.

## Usage

Connecting:
```kotlin
try {
    val connectedSocket = socket.connectAwait()
} catch(t: SocketIOConnectionException) {
    
}
``` 

Emitting Events:
```kotlin
val result = socket.emitAwait("event_name", arguments())
```

Listening for one-time events:
```kotlin
val result = emitter.onceAwait("event_name")
```

Events as a flow:
```kotlin
emitter.onFlow("event_name")
    .collect { event -> doStuff(event) }
```

## Versions

Library is designed, compiled and tested against `io.socket:socket.io-client:1.0.1`.

Currently there is no guarantee of working on older or newer versions.

## Download

Add a gradle dependency to your project:

Groovy

```groovy
repositories {
    mavenCentral()
}
implementation "com.tfowl.socketio:socket-io-coroutines:$socketIoCoroutinesVersion"
```

Kotlin DSL

```kotlin
repositories {
    mavenCentral()
}
implementation("com.tfowl.socketio:socket-io-coroutines:$socketIoCoroutinesVersion")
```

Add a maven dependency to your project:

```xml

<dependency>
    <groupId>com.tfowl.socketio</groupId>
    <artifactId>socket-io-coroutines</artifactId>
    <version>${socketIoCoroutinesVersion}</version>
</dependency>
```

## License

```
MIT License

Copyright (c) 2021 Thomas Fowler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```