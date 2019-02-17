describe('Tile', function() {
	before(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("mirror_results.json").as("results")
	})
	it('Mirror the miners', function() {
		cy.visit("/")
		cy.get(".menu-mirror").click()
		cy.get("h2").should("contain", "Mirror")
		cy.get(".input-blueprint").invoke("val", this.blueprints.miners).type("{rightarrow}=") // hack to fake a paste
		cy.get(".input-mirror-direction input[value='vertically']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.miners_vertically)
		cy.get(".input-mirror-direction input[value='horizontally']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.miners_horizontally)
	})
})
