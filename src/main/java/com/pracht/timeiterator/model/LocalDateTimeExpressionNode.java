package com.pracht.timeiterator.model;

import com.pracht.timeiterator.LocalDateTimeIterator;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LocalDateTimeExpressionNode {
	private LocalDateTimeIterator left;
	
	private LocalDateTimeIterator right;
	
	private ExpressionOperator operator;
	

}
