class @View
  constructor: (@target) ->

  clear: (selector = @target) => $(selector).empty()

  append: (item) =>
    $(item).appendTo @target
    item

  isEntryPoint: => @target is '#entry-point-wrapper'

  hasRows: (model) => model._embedded?

  hasLinks: (model) => model._links?

  createLink: (resource, model) =>
    if model instanceof Link then model else new Link(resource, model)

  #
  #
  # HTML element factory methods
  #
  #
  createDiv: (clazz = "") -> $("<div class='#{clazz}'></div>")

  createSpan: (clazz = "") -> $("<span class='#{clazz}'></span>")

  createOl: (clazz = "") -> $("<ol class='#{clazz}'></ol>")

  createLi: (clazz = "") -> $("<li class='#{clazz}'></li>")

  createParagraph:  -> $("<p></p>")

  createLabel: (text) -> $("<label>#{text}</label>")

  createValue: (value) -> $("<span>#{value}</span>")

  createInput: (id, value="", type="text") => $("<input id='#{id}' type='#{type}' value='#{value}'/>")

  createHyperLink: (text) -> $("<a href='javascript: return false'>#{text}</a>")

  createButton: (value, link = null) ->
    btn = $("<input type='button' value='#{value}'/>")
#    if link? then btn.click -> link.trigger()
#    if link? then btn.click -> link.triggerXML()
    if link? then btn.click -> link.triggerJSON()
    btn

  createFormForModel: (formModel, buttons...) ->
    form = $('<form class="border"></form>')
    form.append @renderPropertyList formModel, 'resource-property-list', true
    form.link(formModel);
    if buttons?
      p = @createParagraph()
      p.appendTo form
      _.each buttons, (btn) -> p.append(btn)
    form

  createLabelAndValue: (label, value, editable=false) ->
    p = @createParagraph()
    p.append @createLabel label
    if editable
      p.append @createInput label, value
    else
      p.append @createValue value
    p

  getLink: (resource, linkModel, data) =>
    if (!linkModel.method?)
      if (linkModel.href.substring(0,4) == "http")
        linkModel.method = "GET"
      else
        elements = linkModel.href.split(" ")
        linkModel.method = elements[0]
        linkModel.href = elements[1]
    linkModel.Id = data.Id
    linkModel.TransactionType = data.TransactionType
    linkModel.DebitAcctNo = data.DebitAcctNo
    linkModel.DebitCurrency = data.DebitCurrency
    linkModel.DebitAmount = data.DebitAmount
    linkModel.CreditAcctNo = data.CreditAcctNo
    @commitLink = @createLink(resource, linkModel)
