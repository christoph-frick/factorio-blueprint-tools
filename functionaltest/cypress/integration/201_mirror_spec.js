function simpleTest(input, resultVert, resultHor) {
	cy.pasteBlueprint(input)
	cy.get(".input-mirror-direction input[value='vertically']").click()
	cy.assertResultBlueprint(resultVert)
	cy.get(".input-mirror-direction input[value='horizontally']").click()
	cy.assertResultBlueprint(resultHor)
}

describe('Mirror', function() {
	beforeEach(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("mirror_results.json").as("results")

		cy.goto(".menu-mirror", "Mirror")
	})

	it('Mirror miners', function() {
		simpleTest(this.blueprints.miners, this.results.miners_vertically, this.results.miners_horizontally)
	})

	it('Mirror with tiles', function() {
		simpleTest(this.blueprints.with_tiles, this.results.with_tiles_vertically, this.results.with_tiles_horizontally)
	})

	it('Mirror tank only rotates it', function() {
		simpleTest(this.blueprints.tank, this.results.tank_vertically, this.results.tank_horizontally)
	})
})
