package org.example.service;

import java.util.List;

import org.example.pojo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.example.mapper.ReviewMapper;
import org.example.pojo.Review;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public int insert(Review rw) {
        return reviewMapper.insert(rw);
    }

    @Override
    public int update(Review rw) {
        return reviewMapper.update(rw);
    }

    @Override
    public int delete(int id) {
        return reviewMapper.delete(id);
    }

    @Override
    public List<Review> findAllReview(Review rw) {
        return reviewMapper.findAllReview(rw);
    }

    @Override
    public List<Integer> findClassroomIdsByRating(int rating) {
        return reviewMapper.findClassroomIdsByRating(rating);
    }

    @Override
    public List<ReviewVO> findReviewsByClassroomId(int classroomId) {
        return reviewMapper.findReviewsByClassroomId(classroomId);
    }
}



