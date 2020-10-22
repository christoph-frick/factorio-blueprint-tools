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
	it('Full and sparse', function() {
		cy.pasteBlueprint("0eNqdk9tugzAMQP/Fz6HifsmvTNMErVdFAgflMg2h/PsC1SbUQQt9ipzEJ8dJPELTWuyVIAN8BHGWpIG/jaDFlep2mjNDj8BBGOyAAdXdFFm6oLoq6cegwdaAYyD83DfwyLGn6UbVpHupzH1y7N4ZIBlhBN5E5mD4INs1qDx9i8Ggl9qnSZpO9aisPGUMBuBBmpwyN1ndseLtch7RYk9jv3VJa3o7VfCPnuw2zcNnpukB0wVtp2n2R/+stQkEaVTGLzy6hHBdNN+LWlhuoIrjVkk1F3wRCs+3DekKuDzuuA9cvfSfkmL5SoI2HikKX/oDq3TfY3M/8kX3M/hCpWdEXEZpkVZFXkRhnuXO/QAojFpz")
		cy.assertResultBlueprint("0eJydk9uOgjAQht9lrqvhfHoVYzYo1TSphbRls8Tw7lvQEoKgU69I4e/HN53OHU68pY1kQkNxB3auhYLicAfFrqLkwzvdNRQKYJregIAob8OqFRWVV1mb5+5EuYaeADPv/qDwe/Jxu5alUE0t9XJz0B8JUKGZZvQhMi66H9HeTlQa+haDQFMrs60Ww18NKs72MYEOil0U7uN+sFqwgu1y3tECQyO2rrrVTTtU8EIP0aaJ98k0cjCd0ZCm8US/lErvmFBUavPh3SF466IJFjWz3ECl7lZhPhZcMUnPj0C0As7cHXHg/Kv7FKbzLjGx0STf++oOrNLNjI3zWMymn8AvlWpEBJkfpVGeJqnvJbFpqWb8OY0vVTx7aI7D2vFSVBfG+VjEMp+75RPPkW99QkcfZH7ywfKtT+Dog8xPPli+9fEdfZD5yQfLtz6eow8yP/lg+ZmddzcfbN76oPnWJ3P0QeYnn9X8se//AZ4KmJE=")
        cy.get(".input-landfill-mode input[value='sparse']").click()
		cy.assertResultBlueprint("0eJydk9uOgyAURf/lPGPj/fYrTTPRShsSRAM4GWP490FbjanaQp8MuFmsA5wBStrhlhMmIR+AXBsmID8PIMidFXSck32LIQcicQ0IWFGPo45VmN95o79OiakEhYDouT/IPYU+Lpe8YKJtuHxd7KsLAswkkQQ/RKZB/8O6usRc048YCNpG6GUNG3fVqCg9RQh6yJ0wOEVqtHph+cflvKP5mobmuppOtt1YwYYeGJvG7ifT0MJ0RTM0jRb6rRDSIUxgLvWPd4fg7ovGpqiV5QEqsbcKsqnginB8fQTCHXBq72gGzr56T0GyviXCDi7Jc796A7t03WNTP+ar7kfwi7mYEH7qhUmYJXHiuXGkr1QS+uzGzT7PXVK12NGCVTdC6VTEpur5nVvmQ7u8tY9rmffM8vP5mPovecPzWfKG/kt+1/+i1D+bJPOT")
	})
})
