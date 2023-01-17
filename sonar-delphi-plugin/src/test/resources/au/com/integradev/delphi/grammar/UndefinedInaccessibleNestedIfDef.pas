unit UndefinedInaccessibleNestedIfDef;

{$ifdef Defined}

interface
const
  StringDef =
    '{$ifdef notDefined}' +
    '{$endif}';

implementation

//{$ifdef notDefined}
//{$endif}

{$endif}

initialization

end.
