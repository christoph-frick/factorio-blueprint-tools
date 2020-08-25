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
	it('Can load blueprint, where preview failed for a "to short color" #9', function() {
		cy.pasteBlueprint(this.blueprints.oddcolor)
		cy.assertResultBlueprint(this.results.oddcolor)
	})
	it('Also work on blueprint books', function() {
		cy.pasteBlueprint(this.blueprints.landfillbook)
		cy.assertResultBlueprint(this.results.landfillbook)
	})
	it('Also works on recursive blueprint books', function() {
		cy.pasteBlueprint(this.blueprints.landfillbookinbook)
		cy.assertResultBlueprint(this.results.landfillbookinbook)
	})
})
