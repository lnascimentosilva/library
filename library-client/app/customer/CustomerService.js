(function() {
	
	var CustomerService = function($http) {
		this.add = function(customer) {
			return $http.post("http://localhost:8080/library/api/users", customer);
		}
		
	};
	
	CustomerService.$inject = [ '$http' ];

	angular.module('library.customer').service('CustomerService', CustomerService);
	
})();