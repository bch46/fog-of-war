package fow.common;

import java.util.Iterator;

/**
 * Courtesy of https://code.google.com/p/yet-another-tree-structure/
 */
public class SceneNodeIter implements Iterator<SceneNode> {

        enum ProcessStages {
                ProcessParent, ProcessChildCurNode, ProcessChildSubNode
        }

        private SceneNode treeNode;

        public SceneNodeIter(SceneNode treeNode) {
                this.treeNode = treeNode;
                this.doNext = ProcessStages.ProcessParent;
                this.childrenCurNodeIter = treeNode.children.iterator();
        }

        private ProcessStages doNext;
        private SceneNode next;
        private Iterator<SceneNode> childrenCurNodeIter;
        private Iterator<SceneNode> childrenSubNodeIter;

        @Override
        public boolean hasNext() {

                if (this.doNext == ProcessStages.ProcessParent) {
                        this.next = this.treeNode;
                        this.doNext = ProcessStages.ProcessChildCurNode;
                        return true;
                }

                if (this.doNext == ProcessStages.ProcessChildCurNode) {
                        if (childrenCurNodeIter.hasNext()) {
                                SceneNode childDirect = childrenCurNodeIter.next();
                                childrenSubNodeIter = childDirect.iterator();
                                this.doNext = ProcessStages.ProcessChildSubNode;
                                return hasNext();
                        }

                        else {
                                this.doNext = null;
                                return false;
                        }
                }
                
                if (this.doNext == ProcessStages.ProcessChildSubNode) {
                        if (childrenSubNodeIter.hasNext()) {
                                this.next = childrenSubNodeIter.next();
                                return true;
                        }
                        else {
                                this.next = null;
                                this.doNext = ProcessStages.ProcessChildCurNode;
                                return hasNext();
                        }
                }

                return false;
        }

        @Override
        public SceneNode next() {
                return this.next;
        }

        @Override
        public void remove() {
                throw new UnsupportedOperationException();
        }

}
