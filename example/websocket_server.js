const http = require('http');
const WebSocketServer = require('websocket').server;
const server = http.createServer();

server.listen(9898);

const wsServer = new WebSocketServer({
  httpServer: server,
});

wsServer.on('request', function (request) {
  const connection = request.accept(null, request.origin);
  connection.on('message', function (message) {
    console.log('Received Message:', message.utf8Data);

    if (message.utf8Data === 'Q1') {
      connection.sendUTF('Q1 reply');
    } else if (message.utf8Data === 'Q2') {
      connection.sendUTF('Q2 reply');
    } else {
      connection.sendUTF('This is the ws://localhost:9898');
    }
  });
  connection.on('close', function (reasonCode, description) {
    console.log('Client has disconnected.');
  });
});
