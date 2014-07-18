DashboardView = require 'views/dashboard'
StatusView = require 'views/status'

RequestsView = require 'views/requests'

RequestView = require 'views/request'

TasksView = require 'views/tasks'

TaskView = require 'views/task'
TailView = require 'views/tail'

RacksView = require 'views/racks'
SlavesView = require 'views/slaves'

PageNotFoundView = require 'views/pageNotFound'

currentViewIsTailView = ->
    Backbone.history.fragment.match(/^task\/.+\/tail\//)?.length is 1

htmlClasses = ->
    if currentViewIsTailView()
        $('html').addClass('tail-view')
    else
        $('html').removeClass('tail-view')

Backbone.history.on 'route', ->
    app.views.nav.render()
    htmlClasses()

windowBlurred = false

$(window).on 'blur', ->
    windowBlurred = true

$(window).on 'focus', ->
    windowBlurred = false
    refresh()

window.globalRefreshTimeout = undefined
globalRefresh = ->
    clearTimeout(window.globalRefreshTimeout) if window.globalRefreshTimeout
    window.globalRefreshTimeout = setInterval ->
        refresh()
    , 20 * 1000

refresh = ->
    return if localStorage.getItem("preventGlobalRefresh") == "true"
    if not $('body > .vex').length and not windowBlurred
        app.views.current?.refresh?()

class Router extends Backbone.Router

    routes:
        '(/)': 'dashboard'
        'status(/)': 'status'
        'requests/:requestsFilter/:requestsSubFilter/:searchFilter(/)': 'requestsFiltered'
        'requests/:requestsFilter/:requestsSubFilter(/)': 'requestsFiltered'
        'requests/:requestsFilter(/)': 'requestsFiltered'
        'requests(/)': 'requestsFiltered'
        'request/:requestId(/)': 'request'
        'tasks/:tasksFilter/:searchFilter(/)': 'tasksFiltered'
        'tasks/:tasksFilter(/)': 'tasksFiltered'
        'tasks(/)': 'tasksFiltered'
        'task/:taskId(/)': 'task'
        # 'task/:taskId/files(/)': 'task'
        'task/:taskId/files(/)*path': 'task'
        'task/:taskId/tail/*path': 'tail'
        'racks(/)': 'racks'
        'slaves(/)': 'slaves'
        '*anything': 'templateFromURLFragment'

    dashboard: ->
        app.showView new DashboardView

    status: ->
        if not app.views.status?
            app.views.status = new StatusView
        app.views.current = app.views.status
        app.show app.views.status.refresh(fromRoute = true)

    requestsFiltered: (requestsFilter = 'all', requestsSubFilter = 'all', searchFilter = '') ->
        if requestsSubFilter is 'running'
            requestsSubFilter = 'daemon' # Front end URL migration :P

        app.views.current = new RequestsView {requestsFilter, requestsSubFilter, searchFilter}
        app.views.current.render()
        app.show app.views.current

    request: (requestId) ->
        app.showView new RequestView requestId: requestId

    tasksFiltered: (tasksFilter = 'active', searchFilter = '') ->
        app.views.current = new TasksView {tasksFilter, searchFilter}

        app.views.current.render()
        app.show app.views.current

    task: (taskId, path) ->
        app.showView new TaskView id: taskId, path: path

    tail: (taskId, path = '') ->
        app.showView new TailView taskId: taskId, path: path

    racks: ->
        if not app.views.racks?
            app.views.racks = new RacksView
            app.views.current = app.views.racks
            app.show app.views.racks.render().refresh()
        else
            app.views.current = app.views.racks
            app.show app.views.racks.refresh()

    slaves: ->
        if not app.views.slaves?
            app.views.slaves = new SlavesView
            app.views.current = app.views.slaves
            app.show app.views.slaves.render().refresh()
        else
            app.views.current = app.views.slaves
            app.show app.views.slaves.refresh()

    templateFromURLFragment: ->
        app.views.current = undefined

        template = undefined
        try
            template = require "../views/templates/#{ Backbone.history.fragment }"
        catch error

        if template
            app.show el: $(template)[0]
            return

        @show404()

    show404: ->
        if not app.views.pageNotFound?
            app.views.pageNotFound = new PageNotFoundView
            app.views.current = app.views.pageNotFound
            app.show app.views.pageNotFound.render()
        else
            app.views.current = app.views.pageNotFound
            app.show app.views.pageNotFound

module.exports = Router
