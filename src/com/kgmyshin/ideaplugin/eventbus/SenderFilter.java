package com.kgmyshin.ideaplugin.eventbus;

import com.intellij.psi.*;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

/**
 * Created by kgmyshin on 2015/06/07.
 */
public class SenderFilter implements Filter {

    public final PsiClass eventClass;

    public SenderFilter(PsiClass eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public boolean shouldShow(Usage usage) {
        PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
        if (element instanceof PsiReferenceExpression) {
            if ((element = element.getParent()) instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpression = (PsiMethodCallExpression) element;
                PsiType[] types = callExpression.getArgumentList().getExpressionTypes();
                for (PsiType type : types) {
                    PsiClass psiClass = PsiUtils.getClass(type, element);
                    if (isTargetEventClass(psiClass)) {
                        // pattern : EventBus.getDefault().post(new Event());
                        return true;
                    }
                }
                if ((element = element.getParent()) instanceof PsiExpressionStatement) {
                    if ((element = element.getParent()) instanceof PsiCodeBlock) {
                        PsiCodeBlock codeBlock = (PsiCodeBlock) element;
                        PsiStatement[] statements = codeBlock.getStatements();
                        for (PsiStatement statement : statements) {
                            if (statement instanceof PsiDeclarationStatement) {
                                PsiDeclarationStatement declarationStatement = (PsiDeclarationStatement) statement;
                                PsiElement[] elements = declarationStatement.getDeclaredElements();
                                for (PsiElement variable : elements) {
                                    if (variable instanceof PsiLocalVariable) {
                                        PsiLocalVariable localVariable = (PsiLocalVariable) variable;
                                        PsiClass psiClass = PsiUtils.getClass(localVariable.getTypeElement().getType(), element);
                                        if (isTargetEventClass(psiClass)) {
                                            // pattern :
                                            //   Event event = new Event();
                                            //   EventBus.getDefault().post(event);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isTargetEventClass(PsiClass psiClass) {
        String psiClassName = psiClass.getName();
        return !"Object".equals(psiClassName) && (psiClassName.equals(eventClass.getName()) || psiClass.isInheritor(eventClass, true));
    }
}
