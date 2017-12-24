/**
 * Testing client-side consumer 
 */
describe('Consumer Controller', function() {
	var $rootScope, createController;
	
	beforeEach(module('consumerApp'));
	
	beforeEach(inject(function($injector) {
		//TODO: Set up the mock for web socket??
		//$httpBackend = $injector.get('$httpBackend');
		// Get hold of a scope (i.e. the root scope)
		$rootScope = $injector.get('$rootScope');
		// The $controller service is used to create instances of controllers
		var $controller = $injector.get('$controller');
		createController = function() {
			return $controller('consumerCtrl', {'$scope' : $rootScope });
		};
	}));
	
	afterEach(function() {
//		$httpBackend.verifyNoOutstandingExpectation();
//		$httpBackend.verifyNoOutstandingRequest();
	});
	
	it('should connect to end point', function() {
		var testToken = 'TESTING!!';
//		$httpBackend.expectPOST(testToken, {mode : testToken, msg: testToken}).respond(testToken);
		$rootScope.endpointUrl = testToken;
		createController();
		$rootScope.message = testToken;
		
//		$httpBackend.flush();
//		expect($rootScope.message).toEqual("???");
	});
});