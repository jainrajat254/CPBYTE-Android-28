package com.example.projecthub

import com.example.projecthub.data.Assignment
import com.example.projecthub.models.user

object dummyData {
    val dummyUser = user(
        userId = "12345",
        name = "John Doe",
        bio = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        collegeName = "XYZ University",
        semester = "3rd",
        collegeLocation = "New York, USA",
        skills = listOf("Kotlin", "Java", "Android Development"),
        profilePhotoId = R.drawable.profilephoto1
    )

//    val dummyAssignments = listOf(
//        Assignment(
//            title = "Android App Development",
//            description = "Create a simple Android app using Kotlin.",
//            subject = "Computer Science",
//            deadline = "2023-12-01",
//            budget = "300",
//            postedBy = "John Doe",
//            timestamp = System.currentTimeMillis()
//        ),
//        Assignment(
//            title = "Web Development Project",
//            description = "Build a responsive website using HTML, CSS, and JavaScript.",
//            subject = "Web Development",
//            deadline = "2023-11-15",
//            budget = "250",
//            postedBy = "Jane Smith",
//            timestamp = System.currentTimeMillis()
//        ),
//        Assignment(
//            title = "Data Science Project",
//            description = "Analyze a dataset and create visualizations using Python.",
//            subject = "Data Science",
//            deadline = "2023-11-30",
//            budget = "200",
//            postedBy = "Alice Johnson",
//            timestamp = System.currentTimeMillis()
//        ),
//        Assignment(
//            title = "Machine Learning Assignment",
//            description = "Implement a machine learning algorithm using Python.",
//            subject = "Machine Learning",
//            deadline = "2023-12-10",
//            budget = "250",
//            postedBy = "Bob Brown",
//            timestamp = System.currentTimeMillis()
//        )
//    )

}