package org.wliamp.gameplay.queue

interface ICriteria {
    /**
     * Kiểm tra xem Criteria này có phù hợp (match) với Criteria khác không.
     *
     * @param other Criteria để so sánh
     * @return true nếu match, false nếu không
     */
    fun matches(other: ICriteria): Boolean
}
