# Getting Started
> __request__ = websocket message __player -> server__\
> __message__ = websocket message __server -> player__

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
