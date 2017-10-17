(function() {

	var customerModule = angular.module('library.customer', ['ngRoute']);
	customerModule.config(function($routeProvider) {
		$routeProvider
			.when('/', {
				controller: 'CustomerCtrl',
				templateUrl: 'app/customer/new-customer.html'
			})
	});
	

})();