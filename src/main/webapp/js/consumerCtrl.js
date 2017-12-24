/**
 * Consumer Controller
 */
app.controller("consumerCtrl", function($scope, $location) {
	$scope.endpointUrl = "ws://localhost:"+$location.port()+"/_testing_/endpoint";
	$scope.message = "";
	
	var webSocket = new WebSocket($scope.endpointUrl);
	
	webSocket.onerror = function (event) {
		$scope.message = event.data;
		$scope.$apply();
	};
	webSocket.onopen = function(event) {
		webSocket.send("Connected");
	};
	webSocket.onmessage = function(event) {
		if (event && event.data) {
			var jsonObj = JSON.parse(event.data);
			$scope.message = $scope.message + '\n' 
				+ jsonObj.timeStamp + ': '+ jsonObj.msg;
			$scope.$apply();
		}
		webSocket.send("listen");
	};
	
	$scope.$on('$destroy', function() {
		webSocket.close(); //Close web socket connection
	});
});
