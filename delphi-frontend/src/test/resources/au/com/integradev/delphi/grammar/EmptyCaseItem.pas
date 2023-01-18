unit EmptyCaseItem;

interface

implementation

procedure EmptyCaseItemTest;
begin
  case SomeVariable of
    Case1 : DoSomething;
    Case2 : DoSomething;
    Case3 : DoSomething;
  end;

  case SomeVariable of
    Case1 : DoSomething;
    Case2 : {Do nothing};
    Case3 : DoSomething;
  end;

  case SomeVariable of
    Case1 : DoSomethingWithoutSemicolon
    Case2 : {Do nothing without semicolon}
    Case3 : DoSomethingWithoutSemicolon
  end;
end;

end.
