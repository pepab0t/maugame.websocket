# Getting Started

> __request__ = websocket message __player -> server__\
> __message__ = websocket message __server -> player__

## Connection

There is few connection options available.\
Base url is: `ws://<host>:<port>/game`\
and possible query parameters:

- `user: string`: player username to be registered in the game
- `player (optional): string`: game assigned playerId, if known, server is able to reconnect player to the original game
- `lobby (optional): string`: lobby name to connect to
    - `new: boolean`: whether to create new lobby, if `false` connect to the existing lobby, defaults to `false`
    - `private: boolean`: whether the newly created lobby is private (meaning players need to know its name to connect)
      or public, defaults to `false`

## websocket requests

### Move requests

```json
{
  "requestType": "MOVE",
  "move": {
    "moveType": "DRAW"
  }
}
```

```json
{
  "requestType": "MOVE",
  "move": {
    "moveType": "PASS"
  }
}
```

```json
{
  "requestType": "MOVE",
  "move": {
    "moveType": "PLAY",
    "card": {
      "color": "HEARTS",
      "type": "SEVEN"
    }
  }
}
```

```json
{
  "requestType": "MOVE",
  "move": {
    "moveType": "PLAY",
    "card": {
      "color": "HEARTS",
      "type": "QUEEN"
    },
    "nextColor": "SPADES"
  }
}
```

### Control requests

#### Ready/Unready lobby request

```json
{
  "requestType": "CONTROL",
  "control": {
    "controlType": "READY"
  }
}
```

```json
{
  "requestType": "CONTROL",
  "control": {
    "controlType": "UNREADY"
  }
}
```

## Messages

### type: __SERVER_MESSAGE__

#### Ready/Unready

```json
{
  "messageType": "SERVER_MESSAGE",
  "body": {
    "bodyType": "READY",
    "username": "joe"
  }
}
```

```json
{
  "messageType": "SERVER_MESSAGE",
  "body": {
    "bodyType": "UNREADY",
    "username": "joe"
  }
}
```

### type: __ERROR__

```json
{
  "messageType": "ERROR",
  "exceptionBody": {
    "name": "ExceptionClassName",
    "message": "This is an exception message.",
    "timestamp": "2025-08-29T07:36:33.204362Z"
  }
}
```
