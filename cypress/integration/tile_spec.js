describe('Tile', function() {
	before(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("tile_results.json").as("results")
	})
	it('Tile the miners 2x2', function() {
		cy.visit("/")
		cy.get(".menu-tile").click()
		cy.get("h2").should("contain", "Tile")
		cy.get(".input-blueprint").invoke("val", this.blueprints.miners).type("{rightarrow}=") // hack to fake a paste
		cy.get(".input-tile-x input").clear().type("2")
		cy.get(".input-tile-y input").clear().type("2")
		cy.get(".input-result-blueprint").should("have.value", this.results.miners2x2)
	})
})
