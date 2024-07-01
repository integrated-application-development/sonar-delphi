unit Expressions;

interface

implementation

{$if 123 = {$i NestedInclude.inc}}
{$endif}

{$if (*$if True*) False (*$endif*)}
{$endif}

{$if 123 = {$ifdef FOO} 123 {$else} 321 {$endif}}
{$endif}

{$if
  123 //}
  = {}
  321 (*}*)
}
{$endif}

{$if
  {$if
    123 //}
    = {}
    321 (*}*)
  }
  {$endif}
}
{$endif}

const
  Bracket = '}';
  ParenBracket = '*)';

{$if
  {$if Bracket = '}'}
  True
  {$endif}
}
{$endif}

{$if
  (*$if ParenBracket = '*)'*)
  True
  (*$endif*)
}
{$endif}

{$if
  {$if Bracket = '}
  }
  True
  {$endif}
}
{$endif}

{$if
  (*$if ParenBracket = '*)
  *)
  True
  (*$endif*)
}
{$endif}

{$if
  {$if Bracket = '''
  }
  '''}
  True
  {$endif}
}
{$endif}

{$if
  (*$if ParenBracket = '''
  *)
  '''*)
  True
  (*$endif*)
}
{$endif}

end.
