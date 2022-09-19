describe('Tile', function() {
	beforeEach(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("tile_results.json").as("results")
		cy.goto(".menu-tile", "Tile")
	})
	it('Tile the belts 2x2', function() {
		cy.pasteBlueprint(this.blueprints.with_tiles)
		cy.get(".input-tile-x input").clear().type("2")
		cy.get(".input-tile-y input").clear().type("2")
		cy.assertResultBlueprint(this.results.with_tiles_2x2)
	})
})
