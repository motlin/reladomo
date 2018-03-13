/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/* Generated By:JJTree: Do not edit this line. ASTIsNullClause.java */


package com.gs.fw.common.mithra.generator.queryparser;

import com.gs.fw.common.mithra.generator.MithraObjectTypeWrapper;

import java.util.List;

public class ASTIsNullClause extends SimpleNode implements LeafLevelExpression, Operator {
    private boolean not;
    public ASTIsNullClause(int id) {
        super(id);
    }

    public ASTIsNullClause(MithraQL p, int id) {
        super(p, id);
    }

    public boolean isNot()
    {
        return not;
    }

    public void setNot(boolean not)
    {
        this.not = not;
    }

    public void addLeafLevelExpressionsToList(List list)
    {
        list.add(this);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(MithraQLVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public boolean involvesThis()
    {
        return getAttribute().belongsToThis();
    }

    private ASTAttributeName getAttribute()
    {
        return ((ASTRelationalExpression) this.parent).getLeft();
    }

    public boolean involvesClassAsNonThis(MithraObjectTypeWrapper owner)
    {
        return !this.involvesThis() && this.getAttribute().getOwner().equals(owner);
    }

    public boolean involvesOnlyThis(MithraObjectTypeWrapper mithraObjectTypeWrapper)
    {
        return this.involvesClassAsNonThis(mithraObjectTypeWrapper);
    }

	public boolean isGreaterThan()
	{
		return false;
	}

	public boolean isLesserThan()
	{
		return false;
	}

	public boolean isGreaterThanOrEqualTo()
	{
		return false;
	}

	public boolean isLesserThanOrEqualTo()
	{
		return false;
	}

    @Override
    public boolean isContains()
    {
        return false;
    }

    @Override
    public boolean isStartsWith()
    {
        return false;
    }

    @Override
    public boolean isEndsWith()
    {
        return false;
    }

    public boolean isEqual()
	{
		return false;
	}

	public boolean isNotEqual()
	{
		return false;
	}

	public boolean isIn()
	{
		return false;
	}

	public boolean isIsNullOrIsNotNull()
	{
		return true;
	}

    @Override
    public boolean isIsNotNull()
    {
        return isNot();
    }

    public Operator getReverseOperator()
	{
		throw new RuntimeException("not implemented");
	}

	public String getMethodName()
	{
        if (isNot()) return "isNotNull";
		return "isNull";
	}

    public String toString()
    {
        return this.getMethodName();
    }

    public boolean equalsOther(SimpleNode other)
    {
        if (other instanceof ASTIsNullClause)
        {
            return ((ASTIsNullClause)other).getMethodName().equals(this.getMethodName());
        }
        return false;
    }

    public boolean isUnary()
    {
        return true;
    }

    public boolean isEqualsEdgePoint()
    {
        return false;
    }

    public String getPrimitiveExpression(String left, String right)
    {
        throw new RuntimeException("not implemented");
    }

    public String getNonPrimitiveExpression(String left, String right)
    {
        throw new RuntimeException("not implemented");
    }
}
