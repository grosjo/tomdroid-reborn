package org.tomdroid.reborn;

// enum holding all types of xml tags known by tomdroid.
// neccesary for the spannable string to xml converter.
public enum TagType { 	
	ROOT,
	LIST,
	LIST_ITEM,
	BOLD,
	ITALIC,
	HIGHLIGHT,
	LINK,
	LINK_INTERNAL,
	TEXT,
	STRIKETHROUGH,
	MONOSPACE,
	SIZE_SMALL,
	SIZE_LARGE,
	SIZE_HUGE,
	MARGIN,
	OTHER
}
