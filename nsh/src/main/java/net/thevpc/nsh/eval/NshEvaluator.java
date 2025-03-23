/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsh.eval;

import net.thevpc.nsh.parser.nodes.NshCommandNode;

/**
 *
 * @author thevpc
 */
public interface NshEvaluator {

    int evalSuffixOperation(String opString, NshCommandNode node, NshContext context);

    int evalSuffixAndOperation(NshCommandNode node, NshContext context);

    int evalBinaryAndOperation(NshCommandNode left, NshCommandNode right, NshContext context);

    int evalBinaryOperation(String opString, NshCommandNode left, NshCommandNode right, NshContext context);

    int evalBinaryOrOperation(NshCommandNode left, NshCommandNode right, NshContext context);

    int evalBinaryPipeOperation(NshCommandNode left, NshCommandNode right, final NshContext context);

    int evalBinarySuiteOperation(NshCommandNode left, NshCommandNode right, NshContext context);

    String evalCommandAndReturnString(NshCommandNode left, NshContext context);


    String evalDollarSharp(NshContext context);

    String evalDollarName(String name, NshContext context);

    String evalDollarInterrogation(NshContext context);

    String evalDollarInteger(int index, NshContext context);

    String evalDollarExpression(String stringExpression, NshContext context);

    String evalSimpleQuotesExpression(String expressionString, NshContext context);

    String evalDoubleQuotesExpression(String stringExpression, NshContext context);

    String evalAntiQuotesExpression(String stringExpression, NshContext context);

    String evalNoQuotesExpression(String stringExpression, NshContext context);

    String expandEnvVars(String stringExpression, boolean escapeResultPath, NshContext context);

}
