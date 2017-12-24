/**
 * Producer Controller
 */
app.controller("producerCtrl", function($scope, $http, $location) {
	$scope.jmsMode = "PUBLISH_TO_TOPIC"; 
	$scope.jpsUrl = "http://localhost:"+$location.port()+"/_testing_/JPS/produce.do";
	$scope.message = "";
	$scope.feedback = "";
	$scope.produce = function() {
		$http.post($scope.jpsUrl, {
			 mode : $scope.jmsMode
			,msg: $scope.message
		})
		.success(function(data, status, headers, config){
			$scope.feedback = "Success: "+data;
		})
		.error(function(data, status, headers, config){
			$scope.feedback = "Error: "+data;
		});
	};
});
