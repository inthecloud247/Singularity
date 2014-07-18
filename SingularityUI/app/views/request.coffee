View = require './view'

ExpandableTableSubview = require './expandableTableSubview'

Request = require '../models/Request'
RequestTasks = require '../collections/RequestTasks'

# Subview collections
RequestHistoricalTasks = require '../collections/RequestHistoricalTasks'
RequestDeployHistory = require '../collections/RequestDeployHistory'
RequestHistory = require '../collections/RequestHistory'

class RequestView extends View

    baseTemplate: require './templates/requestBase'

    headerTemplate: require './templates/requestHeader'

    activeTasksTemplate: require './templates/requestActiveTasks'
    scheduledTasksTemplate: require './templates/requestScheduledTasks'

    # Subview templates
    historicalTasksTemplate: require './templates/requestHistoricalTasks'
    deployHistoryTemplate: require './templates/requestDeployHistory'
    requestHistoryTemplate: require './templates/requestHistory'

    events: ->
        _.extend super,
            'click [data-action="viewJSON"]': 'viewJson'
            'click [data-action="viewObjectJSON"]': 'viewObjectJson'
            'click [data-action="viewRequestHistoryJSON"]': 'viewRequestHistoryJson'

            'click [data-action="remove"]': 'removeRequest'
            'click [data-action="run-request-now"]': 'runRequest'
            'click [data-action="pause"]': 'pauseRequest'
            'click [data-action="unpause"]': 'unpauseRequest'
            'click [data-action="bounce"]': 'bounceRequest'

            'click [data-action="run-now"]': 'runTask'

            'click [data-action="expand-deploy-history"]': 'expandDeployHistory'

    initialize: ({@requestId}) ->
        # Base Request we get a bunch of info from
        @model = new Request id: @requestId
        @listenTo @model, 'sync',  @renderHeader
        @listenTo @model, 'error', @handleAjaxError

        # @activeDeploy = new RequestActiveDeploy {@requestId}
        # @listenTo @activeDeploy, 'sync',  @renderActiveDeploy
        # @listenTo @activeDeploy, 'error', @handleAjaxError

        # List of tasks currently running
        @collections.activeTasks = new RequestTasks [],
            requestId: @requestId,
            state: 'active'
        @listenTo @collections.activeTasks, 'sync',  @renderActiveTasks
        @listenTo @collections.activeTasks, 'error', @handleAjaxError

        # Tasks that are scheduled
        # @scheduledTasks = new RequestTasks [],
        #     requestId: @requestId,
        #     state: 'scheduled'
        # @listenTo @scheduledTasks, 'sync',  @renderScheduledTasks
        # @listenTo @scheduledTasks, 'error', @handleAjaxError

        # Here be subviews!
        #
        # The subviews are fed a collection and template and take care
        # of everything themselves
        #
        @collections.requestHistory = new RequestHistory [], {@requestId}
        # We want this for the header too!
        @listenTo @collections.requestHistory, 'sync', @renderHeader

        @collections.historicalTasks = new RequestHistoricalTasks [], {@requestId}
        @collections.deployHistory = new RequestDeployHistory [], {@requestId}

        @subviews.historicalTasks = new ExpandableTableSubview
            collection: @collections.historicalTasks
            template:   @historicalTasksTemplate

        @subviews.deployHistory = new ExpandableTableSubview
            collection: @collections.deployHistory
            template:   @deployHistoryTemplate

        @subviews.requestHistory = new ExpandableTableSubview
            collection: @collections.requestHistory
            template:   @requestHistoryTemplate

        @refresh()

    refresh: ->
        @model.fetch()
        @collections.requestHistory.fetch() # Also used by subview
        @collections.activeTasks.fetch()
        # @scheduledTasks.fetch()

        # Subview collections
        @collections.historicalTasks.fetch()
        @collections.deployHistory.fetch()

    render: ->
        @$el.html @baseTemplate()

        # Attach subview elements
        @$('.historical-tasks-container').html @subviews.historicalTasks.$el
        @$('.deploy-history-container').html   @subviews.deployHistory.$el
        @$('.request-history-container').html  @subviews.requestHistory.$el

    renderHeader: ->
        context = request: @model.attributes

        if @collections.requestHistory.synced
            context.firstRequestHistoryItem = @collections.requestHistory.first().attributes

        @$('.header-container').html @headerTemplate context

    renderStatus: ->
        # This is the bit right of the state badge in the header
        @renderHeader() unless not @$('.request-status').is ':empty'

    renderActiveTasks: ->
        @$('.active-tasks-container').html @activeTasksTemplate
            activeTasks: _.pluck @collections.activeTasks.models, 'attributes'

    # renderScheduledTasks: ->
    #     @$('.scheduled-tasks-container').html @scheduledTasksTemplate
    #         scheduledTasks: _.pluck @scheduledTasks.models, 'attributes'

    handleAjaxError: (stuffWeDontCareAbout, response) =>
        if response.status is 404
            app.caughtError()
            @$el.html "<h1>Request not found.</h1>"

    viewJson: (e) =>
        utils.viewJSON 'task', $(e.target).data('task-id')

    viewObjectJson: (e) =>
        utils.viewJSON 'request', $(e.target).data('request-id')

    viewRequestHistoryJson: (e) =>
        utils.viewJSON 'requestHistory', $(e.target).data('local-request-history-id')

    removeRequest: (e) =>
        @model.promptRemove =>
            app.router.navigate 'requests', trigger: true

    runRequest: (e) =>
        @model.promptRun =>
            @refresh()

    pauseRequest: (e) =>
        @model.promptPause =>
            @refresh()

    unpauseRequest: (e) =>
        @model.promptUnpause =>
            @refresh()
    
    bounceRequest: (e) =>
        @model.promptBounce =>
            @refresh()

    runTask: (e) =>
        $row = $(e.target).parents('tr')
        $containingTable = $row.parents('table')
        taskModel = app.collections.tasksScheduled.get($(e.target).data('task-id'))
        
        @model.promptRun =>
            app.collections.tasksScheduled.remove(taskModel)
            $row.remove()
            utils.handlePotentiallyEmptyFilteredTable $containingTable, 'task'

    expandDeployHistory: ->
        @subviews.deployHistory.expand()

module.exports = RequestView
