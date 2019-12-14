import java.util.*;

abstract class AI {
	abstract public void move(Board board);

	public int presearch_depth = 3;
	public int normal_depth = 15;
	public int wld_depth = 15;
	public int perfect_depth = 13;

}

class AlphaBetaAI extends AI {
	class Move extends Point {
		public int eval = 0;

		public Move() {
			super(0, 0);
		}

		public Move(int x, int y, int e) {
			super(x, y);

			eval = e;
		}
	};

	private Evaluator Eval = null;

	public void move(Board board) {
		BookManager book = new BookManager();
		Vector movables = book.find(board);

		if (movables.isEmpty()) {
			// �łĂ�ӏ����Ȃ���΃p�X����
			board.pass();
			return;
		}

		if (movables.size() == 1) {
			// �łĂ�ӏ�����J�������Ȃ�T���͍s�킸�A�����ɑł��ĕԂ�
			board.move((Point) movables.get(0));
			return;
		}

		int limit;
		Eval = new MidEvaluator();
		sort(board, movables, presearch_depth); // ���O�Ɏ��ǂ������ȏ��Ƀ\�[�g

		if (Board.MAX_TURNS - board.getTurns() <= wld_depth) {
			limit = Integer.MAX_VALUE;
			if (Board.MAX_TURNS - board.getTurns() <= perfect_depth)
				Eval = new PerfectEvaluator();
			else
				Eval = new WLDEvaluator();
		} else {
			limit = normal_depth;
		}

		int eval, eval_max = Integer.MIN_VALUE;
		Point p = null;
		for (int i = 0; i < movables.size(); i++) {
			board.move((Point) movables.get(i));
			eval = -alphabeta(board, limit - 1, -Integer.MAX_VALUE, -Integer.MIN_VALUE);
			board.undo();

			if (eval > eval_max)
				p = (Point) movables.get(i);
		}

		board.move(p);

	}

	private int alphabeta(Board board, int limit, int alpha, int beta) {
		// �[�������ɒB������]���l��Ԃ�
		if (board.isGameOver() || limit == 0)
			return evaluate(board);

		Vector pos = board.getMovablePos();
		int eval;

		if (pos.size() == 0) {
			// �p�X
			board.pass();
			eval = -alphabeta(board, limit, -beta, -alpha);
			board.undo();
			return eval;
		}

		for (int i = 0; i < pos.size(); i++) {
			board.move((Point) pos.get(i));
			eval = -alphabeta(board, limit - 1, -beta, -alpha);
			board.undo();

			alpha = Math.max(alpha, eval);

			if (alpha >= beta) {
				// ������
				return alpha;
			}
		}

		return alpha;

	}

	private void sort(Board board, Vector movables, int limit) {
		Vector moves = new Vector();

		for (int i = 0; i < movables.size(); i++) {
			int eval;
			Point p = (Point) movables.get(i);

			board.move(p);
			eval = -alphabeta(board, limit - 1, -Integer.MAX_VALUE, Integer.MAX_VALUE);
			board.undo();

			Move move = new Move(p.x, p.y, eval);
			moves.add(move);
		}

		// �]���l�̑傫�����Ƀ\�[�g(�I���\�[�g)

		int begin, current;
		for (begin = 0; begin < moves.size() - 1; begin++) {
			for (current = 1; current < moves.size(); current++) {
				Move b = (Move) moves.get(begin);
				Move c = (Move) moves.get(current);
				if (b.eval < c.eval) {
					// ����
					moves.set(begin, c);
					moves.set(current, b);
				}
			}
		}
		// ���ʂ̏����߂�

		movables.clear();
		for (int i = 0; i < moves.size(); i++) {
			movables.add(moves.get(i));
		}

		return;

	}

	private int evaluate(Board board) {
		return 0;
	}
}
