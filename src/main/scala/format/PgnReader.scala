package lila.chess
package format

object PgnReader {

  def apply(pgn: String): Valid[Replay] = for {
    parsed ← PgnParser(pgn)
    game ← makeGame(parsed.tags)
    replay ← parsed.sans.foldLeft(Replay(game).success: Valid[Replay]) {
      case (replayValid, san) ⇒ for {
        replay ← replayValid
        move ← san(replay.game)
      } yield new Replay(
        game = replay.game(move),
        moves = move :: replay.moves)
    }
  } yield replay

  def makeGame(tags: List[Tag]): Valid[Game] =
    tags collect {
      case Fen(fen) ⇒ fen
    } match {
      case Nil ⇒ Game().success
      case fen :: Nil ⇒ (Forsyth << fen).fold(
        s ⇒ Game(board = s.board, player = s.color).success,
        "Invalid fen %s".format(fen).failNel
      )
      case many ⇒ "Multiple fen tags".failNel
    }
}