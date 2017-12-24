/**
 * Testing client-side producer 
 */
describe('Producer Controller', function() {
	var $httpBackend, $rootScope, createController;
	
	beforeEach(module('producerApp'));
	
	beforeEach(inject(function($injector) {
		// Set up the mock http service responses
		$httpBackend = $injector.get('$httpBackend');
		// Get hold of a scope (i.e. the root scope)
		$rootScope = $injector.get('$rootScope');
		// The $controller service is used to create instances of controllers
		var $controller = $injector.get('$controller');
		createController = function() {
			return $controller('producerCtrl', {'$scope' : $rootScope });
		};
	}));
	
	afterEach(function() {
		$httpBackend.verifyNoOutstandingExpectation();
		$httpBackend.verifyNoOutstandingRequest();
	});
	
	it('should send a successful post message to JMS Producer Servlet (JPS)', function() {
		var testToken = 'TESTING!!';
		$httpBackend.expectPOST(testToken, {mode : testToken, msg: testToken}).respond(testToken);
		createController();
		$rootScope.jmsMode = testToken;
		$rootScope.jpsUrl = testToken;
		$rootScope.message = testToken;
		$rootScope.produce();
		$httpBackend.flush();
		expect($rootScope.feedback).toEqual("Success: "+testToken);
	});
	
	it('should fail to send a post message to JMS Producer Servlet (JPS)', function() {
		var testToken = 'NEGATIVE TEST!!';
		$httpBackend.expectPOST(testToken, {mode : testToken, msg: testToken}).respond(401, testToken);
		createController();
		$rootScope.jmsMode = testToken;
		$rootScope.jpsUrl = testToken;
		$rootScope.message = testToken;
		$rootScope.produce();
		$httpBackend.flush();
		expect($rootScope.feedback).toEqual("Error: "+testToken);
	});
});