
package com.sis.android.data.repository

import com.sis.android.data.model.ManagedStaff

interface StaffManagementRepository {
    fun getStaff(): List<ManagedStaff>

    fun addStaff(staff: ManagedStaff)

    fun updateStaff(staff: ManagedStaff)

    fun setActive(id: Int, isActive: Boolean)
}

class InMemoryStaffManagementRepository :
    StaffManagementRepository {

    private val staffList = mutableListOf(
        ManagedStaff(
            id = 1,
            fullName = "Trần Thị Bình",
            email = "binh.tran@warehouse.vn",
            branch = "Kho Trung tâm",
            role = "STAFF",
            isActive = true
        ),
        ManagedStaff(
            id = 2,
            fullName = "Lê Minh Khoa",
            email = "khoa.le@warehouse.vn",
            branch = "Kho Chi nhánh 1",
            role = "MANAGER",
            isActive = true
        ),
        ManagedStaff(
            id = 3,
            fullName = "Phạm Thu Lan",
            email = "lan.pham@warehouse.vn",
            branch = "Kho Chi nhánh 2",
            role = "STAFF",
            isActive = false
        )
    )

    override fun getStaff(): List<ManagedStaff> {
        return staffList.toList()
    }

    override fun addStaff(staff: ManagedStaff) {
        val newId = (staffList.maxOfOrNull { it.id } ?: 0) + 1
        staffList.add(staff.copy(id = newId))
    }

    override fun updateStaff(staff: ManagedStaff) {
        val index = staffList.indexOfFirst {
            it.id == staff.id
        }

        if (index != -1) {
            staffList[index] = staff
        }
    }

    override fun setActive(id: Int, isActive: Boolean) {
        val index = staffList.indexOfFirst {
            it.id == id
        }

        if (index != -1) {
            staffList[index] =
                staffList[index].copy(isActive = isActive)
        }
    }
}
