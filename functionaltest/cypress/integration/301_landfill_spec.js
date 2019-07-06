describe('Landfill', function() {
	beforeEach(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("landfill_results.json").as("results")
		cy.goto(".menu-landfill", "landfill")
	})
	it('Landfill under the belts', function() {
		cy.pasteBlueprint(this.blueprints.belts)
		cy.assertResultBlueprint(this.results.landfilledbelts)
	})
})
