# Getting Started

## websocket requests
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
