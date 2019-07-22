describe('Split', function() {
	beforeEach(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("split_results.json").as("results")
		cy.goto(".menu-split", "Split")
	})
	it('Split a huge blueprint into 2x2', function() {
		cy.pasteBlueprint(this.blueprints.huge)
		// XXX: the clear here deadlocks chromium
		// cy.get(".input-split-tile-size input").clear().type("68")
		let ts = cy.get(".input-split-tile-size input")
		ts.should("have.value", "64")
		ts.type("{end}{backspace}8")
		cy.assertResultBlueprint(this.results.huge2x2)
	})
})
