describe('Mirror', function() {
	before(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("mirror_results.json").as("results")
	})

	it('Mirror', function() {
		cy.visit("/")
		cy.get(".menu-mirror").click()
		cy.get("h2").should("contain", "Mirror")
		// miners
		cy.get(".input-blueprint").invoke("val", this.blueprints.miners).type("{rightarrow}=") // hack to fake a paste
		cy.get(".input-mirror-direction input[value='vertically']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.miners_vertically)
		cy.get(".input-mirror-direction input[value='horizontally']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.miners_horizontally)
		// tiles
		cy.get(".input-blueprint").invoke("val", this.blueprints.with_tiles).type("{rightarrow}=") // hack to fake a paste
		cy.get(".input-mirror-direction input[value='vertically']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.with_tiles_vertically)
		cy.get(".input-mirror-direction input[value='horizontally']").click()
		cy.get(".input-result-blueprint").should("have.value", this.results.with_tiles_horizontally)
	})
})
