describe('Does it actually run', function() {
	it('Homepage has content', function() {
		cy.visit("/")
		cy.contains("Instructions")
	})
})
