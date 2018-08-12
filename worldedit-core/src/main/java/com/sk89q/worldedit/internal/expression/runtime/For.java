/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.expression.runtime;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.parser.ParserException;

public class For extends Node {
    RValue init;
    RValue condition;
    RValue increment;
    RValue body;

    public For(int position, RValue init, RValue condition, RValue increment, RValue body) {
        super(position);
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    public double getValue() throws EvaluationException {

        int iterations = 0;
        double ret = 0.0D;
        this.init.getValue();

        for(; this.condition.getValue() > 0.0D; this.increment.getValue()) {

            if(iterations > 256) {
                throw new EvaluationException(this.getPosition(), "Loop exceeded 256 iterations.");
            }

            if(Thread.currentThread().isInterrupted()){
                throw new EvaluationException(this.getPosition(), "Thread has been interrupted.");
            }

            ++iterations;

            try {
                ret = this.body.getValue();
            } catch (BreakException var5) {
                if(!var5.doContinue) {
                    return ret;
                }
            }
        }

        return ret;
    }

    public char id() {
        return 'F';
    }

    public String toString() {
        return "for (" + this.init + "; " + this.condition + "; " + this.increment + ") { " + this.body + " }";
    }

    public RValue optimize() throws EvaluationException {
        final RValue newCondition = condition.optimize();
        if (newCondition instanceof Constant && newCondition.getValue() <= 0) {
            // If the condition is always false, the loop can be flattened.
            // So we run the init part and then return 0.0.
            return new Sequence(getPosition(), init, new Constant(getPosition(), 0.0)).optimize();
        }
        return new For(getPosition(), init.optimize(), newCondition, increment.optimize(), body.optimize());
    }

    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        this.init = this.init.bindVariables(expression, false);
        this.condition = this.condition.bindVariables(expression, false);
        this.increment = this.increment.bindVariables(expression, false);
        this.body = this.body.bindVariables(expression, false);
        return this;
    }

    public static Class<For> inject() {
        return For.class;
    }
}

