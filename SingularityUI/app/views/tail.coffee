View = require './view'

class TailView extends View

    pollingTimeout: 3000

    template: require '../templates/tail'

    linesTemplate: require '../templates/tailLogLines'

    events: ->
        _.extend super,
            'click .tail-top-button': 'goToTop'
            'click .tail-bottom-button': 'goToBottom'

    initialize: ({@taskId, @path, firstRequest}) ->
        @filename = _.last @path.split '/'

        @listenTo @collection, 'reset',       @dumpContents
        @listenTo @collection, 'sync',        @renderLines
        @listenTo @collection, 'initialdata', @afterInitialData

        # For the visual loading indicator thing
        @listenTo @collection, 'request', =>
            @$el.addClass 'fetching-data'
        @listenTo @collection, 'sync', =>
            @$el.removeClass 'fetching-data'

    handleAjaxError: (response) =>
        # ATM we get 404s if we request dirs and 500s if the file doesn't exist
        if response.status in [404, 500]
            app.caughtError()
            @$el.html "<h1>Could not get request file.</h1>"

    render: =>
        breadcrumbs = utils.pathToBreadcrumbs @path

        @$el.html @template {@taskId, @filename, breadcrumbs}

        @$contents = @$ '.tail-contents'

        # Attach scroll event manually because Backbone is poopy about it
        @$contents.on 'scroll', @handleScroll

        # Some stuff in the app can change this stuff. We wanna reset it
        $('html, body').css 'min-height', '0px'
        $('#global-zeroclipboard-html-bridge').css 'top', '1px'

    renderLines: ->
        # So we want to either prepend (fetchPrevious) or append (fetchNext) the lines
        # Well, or just render them if we're starting fresh
        $firstLine = @$contents.find '.line:first-child'
        $lastLine  = @$contents.find '.line:last-child'

        # If starting fresh
        if $firstLine.length is 0
            @$contents.html @linesTemplate lines: @collection.toJSON()
        else
            firstLineOffset = parseInt $firstLine.data 'offset'
            lastLineOffset  = parseInt $lastLine.data 'offset'
            # Prepending
            if @collection.getMinOffset() < firstLineOffset
                # Get only the new lines
                lines = @collection.filter (line) => line.get('offset') < firstLineOffset
                @$contents.prepend @linesTemplate lines: _.pluck lines, 'attributes'
                # Gonna need to scroll back to the previous `firstLine` after otherwise
                # we end up at the top again
                @$contents.scrollTop $firstLine.offset().top
            # Appending
            else if @collection.getMaxOffset() > lastLineOffset
                # Get only the new lines
                lines = @collection.filter (line) => line.get('offset') > lastLineOffset
                @$contents.append @linesTemplate lines: _.pluck lines, 'attributes'

    scrollToTop:    => @$contents.scrollTop 0
    scrollToBottom: =>
        scroll = => @$contents.scrollTop @$contents[0].scrollHeight
        scroll()

        # `preventFetch` will prevent the scroll-triggered fetch for
        # happening for 100 ms. This is to prevent a bug that can
        # happen if you have a REALLY busy log file
        @preventFetch = true
        setTimeout =>
            scroll()
            delete @preventFetch
        , 100

    # Get rid of all lines. Used when collection is reset
    dumpContents: -> @$contents.empty()

    handleScroll: (event) =>
        # `Debounce` on animation requests so we only do this when the
        # browser is ready for it
        if @frameRequest?
            cancelAnimationFrame @frameRequest
        @frameRequest = requestAnimationFrame =>
            scrollTop = @$contents.scrollTop()
            scrollHeight = @$contents[0].scrollHeight
            contentsHeight = @$contents.height()

            atBottom = scrollTop >= scrollHeight - contentsHeight
            atTop = scrollTop is 0

            if atBottom and not atTop
                if @collection.moreToFetch
                    return if @preventFetch
                    @collection.fetchNext()
                else
                    @startTailing()
            else
                @stopTailing()

            if atTop and @collection.getMinOffset() isnt 0
                @collection.fetchPrevious()

    afterInitialData: =>
        setTimeout =>
            @scrollToBottom()
        , 150

        @startTailing()

    startTailing: =>
        @scrollToBottom()

        clearInterval @tailInterval
        @tailInterval = setInterval =>
            @collection.fetchNext().done @scrollToBottom
        , @pollingTimeout

        # The class is for CSS stylin' of certain stuff
        @$el.addClass 'tailing'

    stopTailing: ->
        clearInterval @tailInterval
        @$el.removeClass 'tailing'

    remove: ->
        clearInterval @tailInterval
        @$contents.off 'scroll'
        super

    goToTop: =>
        @collection.reset()
        @collection.fetchFromStart().done @scrollToTop
    
    goToBottom: =>
        @collection.reset()
        @collection.fetchInitialData()

module.exports = TailView
