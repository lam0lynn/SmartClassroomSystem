package org.example.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import org.example.pojo.Review;
import org.example.pojo.ReviewVO;

@Mapper
public interface ReviewMapper {
	
	public int insert(Review rw);
	
	public int update(Review rw);
	
	public int delete(int id);
	
	public List<Review> findAllReview(Review rw);
	
	public List<Integer> findClassroomIdsByRating(int rating);

	public List<ReviewVO> findReviewsByClassroomId(int classroomId);
}

