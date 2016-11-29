package com.crayoncms.page

import com.crayoncms.page.enums.PageStatus
import com.crayoncms.user.RoleGroup
import com.crayoncms.theme.Layout

class Page {

	String name
	String slug
	String content
	PageStatus status
	Layout layout
	String keywords
	String description
	RoleGroup roleGroup
	String bodyCss
	Date dateCreated
	Date lastUpdated

    static constraints = {
		name maxSize: 160, blank: false
		slug maxSize: 170, unique: true, blank: false
		layout nullable: false, blank: false
		content widget: 'textarea', nullable: true, blank: true
		description maxSize: 250, blank: true, widget: 'textarea'
		keywords maxSize: 200, blank: true
		roleGroup nullable: false, blank: false
		bodyCss blank: true, maxSize: 50
    }
	
	static mapping = {
		content type: 'text'
	}
}
