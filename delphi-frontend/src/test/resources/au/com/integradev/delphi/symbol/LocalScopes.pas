unit LocalScopes;

interface

implementation

procedure TestNoDuplicatedDeclarationException;
label Baz;
begin
  begin
    var Foo := 'bar';
  end;

  if True then
     var Foo := 'bar'
  else
    var Foo := 'bar';

  case True of
    True: var Foo := 'bar';
    False: var Foo := 'bar';
    else var Foo := 'bar';
  end;

  repeat
     var Foo := 'bar';
  until False;

  while False do begin
      var Foo := 'bar';
  end;

  try
    var Foo := 'bar';
  finally
    var Foo := 'bar';
  end;

  try
    var Foo := 'bar';
  except
    var Foo := 'bar';
  end;

  try
    var Foo := 'bar';
  except
    on TObject do begin
      var Foo := 'bar';
    end
    else
      var Foo := 'bar';
  end;

  while True do
    var Foo := 'bar';

  Baz:
    var Foo := 'bar';

  var Foo := 'bar';
end;

procedure TestLabelScopeFlatteningBehavior;
label Baz, Flarp;
begin
  Baz:
    var Foo := 'bar';
  WriteLn(Foo);

  begin
    Flarp:
      var Boop := 'beep';
    WriteLn(Boop);
  end;
end;

end.