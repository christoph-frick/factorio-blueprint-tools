describe('Landfill', function() {
	before(function() {
		cy.fixture("blueprints.json").as("blueprints")
		cy.fixture("landfill_results.json").as("results")
	})
	it('Landfill under the belts', function() {
		cy.visit("/")
		cy.get(".menu-landfill").click()
		cy.get("h2").should("contain", "landfill")
		cy.get(".input-blueprint").invoke("val", this.blueprints.belts).type("{rightarrow}=") // hack to fake a paste
		cy.get(".input-result-blueprint").should("have.value", this.results.landfilledbelts)
	})
})
