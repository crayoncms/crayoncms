package com.crayoncms

import com.crayoncms.user.RoleGroup
import com.crayoncms.user.UserRoleGroup
import grails.plugin.springsecurity.SpringSecurityUtils
import org.grails.plugins.BinaryGrailsPlugin

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)
class PageController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def pluginManager
    def groovyPagesTemplateEngine
    def springSecurityService

	@Secured(["ROLE_CRAYONCMS_PAGE_CREATE", "ROLE_CRAYONCMS_PAGE_EDIT", "ROLE_CRAYONCMS_PAGE_DELETE"])
    def index(Integer max) {
        params.max = Math.min(max ?: 20, 100)
        respond Page.list(params), model:[pageCount: Page.count()]
    }

	@Secured("permitAll")
    def show(String slug) {
		Page page = Page.findBySlug(slug ?: "home")
        if(page) {

            // First see if the current user has access to the requested page
            if(springSecurityService.isLoggedIn()) {
                def user = springSecurityService.currentUser
                if (!user.authorities.contains(RoleGroup.findByName("Administrator"))
                        && !UserRoleGroup.exists(user?.id, page?.roleGroup?.id)) {
                    notFound() // TODO: see if this can be shown with forbidden.
                    return
                }
            }

            // Now lets merge bind content with layout and prepare html
            def binding = [:] << [content: page.content.encodeAsRaw()]
            def template = groovyPagesTemplateEngine
                                .createTemplate(new ByteArrayInputStream(page?.layout?.code?.getBytes("UTF-8")))
                                .make(binding)

            render view: "/index", model: [
                    content: template, title: page.name, bodyCss: page.bodyCss,
                    description: page.description, keywords: page.keywords
            ]
        } else {
            notFound()
            return
        }
    }

	@Secured("ROLE_CRAYONCMS_PAGE_CREATE")
    def create() {
        def page = new Page(params)

        def activeTheme = pluginManager.getGrailsPlugin("helios-theme")
        if(activeTheme instanceof BinaryGrailsPlugin) {
            def files = new File(activeTheme.getProjectDirectory(), "grails-app/views").list()
            page.layout = files as List
        }

        respond page
    }

	@Secured("ROLE_CRAYONCMS_PAGE_CREATE")
    @Transactional
    def save(Page page) {
        if (page == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (page.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond page.errors, view:'create'
            return
        }

        page.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'page.label', default: 'Page'), page.name])
				flash.outcome = "success"
                //redirect page
				redirect action: "index"
            }
            '*' { respond page, [status: CREATED, view: "index"] }
        }
    }

	@Secured("ROLE_CRAYONCMS_PAGE_EDIT")
    def edit(Page page) {
        respond page
    }

	@Secured("ROLE_CRAYONCMS_PAGE_EDIT")
    @Transactional
    def update(Page page) {
        if (page == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (page.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond page.errors, view:'edit'
            return
        }

        page.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'page.label', default: 'Page'), page.name])
				flash.outcome = "success"
                //redirect page
				redirect action: "index"
            }
            '*'{ respond page, [status: OK] }
        }
    }

	@Secured("ROLE_CRAYONCMS_PAGE_DELETE")
    @Transactional
    def delete(Page page) {

        if (page == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        page.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'page.label', default: 'Page'), page.name])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'page.label', default: 'Page'), params.name])
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
