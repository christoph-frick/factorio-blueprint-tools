describe('Does it actually run', function() {
	it('Homepage has content', function() {
		cy.visit("/index.html")
		cy.contains("Instructions")
	})
})
