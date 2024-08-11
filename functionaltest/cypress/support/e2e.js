// https://on.cypress.io/custom-commands

Cypress.Commands.add("goto", (query, title) => {
	cy.visit("/index.html")
	cy.get("li[data-menu-id$="+query+"]").click()
	cy.get("h2").should("contain", title)
})

Cypress.Commands.add("fakePaste", {prevSubject: true}, (subject, content) => {
	return cy.wrap(subject)
        .focus()
        .clear()
        .invoke("val", content)
        .type("{rightarrow}=")
})

Cypress.Commands.add("pasteBlueprint", (content) => {
	return cy.get(".input-blueprint textarea").fakePaste(content)
})

Cypress.Commands.add("assertResultBlueprint", (content) => {
	cy.get("textarea.input-result-blueprint").should("have.value", content)
})
