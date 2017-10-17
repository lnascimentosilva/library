(function() {
	
	var CustomerCtrl = function($scope, $window, CustomerService) {
		
		$scope.save = function() {
			$scope.customer.type = 'CUSTOMER';
			var result = CustomerService.add($scope.customer);

			result
				.success(function(data) {
					$window.alert('Customer registered successfully');
				})
				.error(function(data, status, headers, config) {
					response = 'StatusCode: ' + status + '\n';
					response += 'Error: ' + data.errorIdentification + ' - ' + data.errorDescription; 					
					$window.alert(response);
				});
		}

	};
	
	CustomerCtrl.$inject = ['$scope', '$window', 'CustomerService'];
	
	angular.module('library.customer').controller('CustomerCtrl', CustomerCtrl);
	
})();